package com.example.userservice.Exception;


import com.example.userservice.Common.CommonResponse.ApiResponse;
import com.example.userservice.Common.CommonUtil.CommonUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandlerController extends ResponseEntityExceptionHandler {
    /** ----------------------------------------------------------------------------------------------------------
     * Handle MissingServletRequestParameterException. Triggered when a 'required' request parameter is missing.
     *
     * @param ex      MissingServletRequestParameterException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     * ---------------------------------------------------------------------------------------------------------*/
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {  String error = ex.getParameterName() + " parameter is missing";
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        return ApiResponse.ApiError(BAD_REQUEST, error, ex);
    }

    /** ----------------------------------------------------------------------------------------------------------
     * Handle HttpMediaTypeNotSupportedException. This one triggers when JSON is invalid as well.
     *
     * @param ex      HttpMediaTypeNotSupportedException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     * ----------------------------------------------------------------------------------------------------------*/
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));
        return ApiResponse.ApiError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, builder.substring(0, builder.length() - 2), ex);
    }



    /**
     * Handle MethodArgumentNotValidException. Triggered when an object fails @Valid validation.
     *
     * @param ex      the MethodArgumentNotValidException that is thrown when @Valid validation fails
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        List<Object> errors = new ArrayList<>();

        errors.add(ex.getBindingResult().getFieldErrors());
        errors.add(ex.getBindingResult().getGlobalErrors());

        return ApiResponse.ApiErrorWithSubError(BAD_REQUEST,"Validation error",ex,errors);
    }

    /**
     * Handles javax.validation.ConstraintViolationException. Thrown when @Validated fails.
     *
     * @param ex the ConstraintViolationException
     * @return the ApiError object
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected void handleConstraintViolation(
            ConstraintViolationException ex) {
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        ApiResponse.ApiErrorWithSubError(BAD_REQUEST,"Validation error",ex,ex.getConstraintViolations());
    }

    /**
     * Handles EntityNotFoundException. Created to encapsulate errors with more detail than javax.persistence.EntityNotFoundException.
     *
     * @param ex the EntityNotFoundException
     * @return the ApiError object
     */
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        return ApiResponse.ApiError(BAD_REQUEST, ex.getMessage(), ex);
    }

    /**
     * Handle HttpMessageNotReadableException. Happens when request JSON is malformed.
     *
     * @param ex      HttpMessageNotReadableException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        return ApiResponse.ApiError(HttpStatus.BAD_REQUEST, "Malformed JSON request", ex);
    }

    /**
     * Handle HttpMessageNotWritableException.
     *
     * @param ex      HttpMessageNotWritableException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        return ApiResponse.ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Error writing JSON output", ex);
    }

    /**
     * Handle NoHandlerFoundException.
     *
     * @param ex
     * @param headers
     * @param status
     * @param request
     * @return
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        return ApiResponse.ApiError(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Could not find the %s method for URL %s", ex.getHttpMethod(), ex.getRequestURL()), ex);
    }

    /**
     * Handle DataIntegrityViolationException, inspects the cause for different DB causes.
     *
     * @param ex the DataIntegrityViolationException
     * @return the ApiError object
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        if (ex.getCause() instanceof ConstraintViolationException) {
            ApiResponse.ApiError(HttpStatus.CONFLICT, "Database error", ex.getCause());
        }
        return ApiResponse.ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getCause());
    }

    /**
     * Handle Exception, handle generic Exception.class
     *
     * @param ex the Exception
     * @return the ApiError object
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        String message = String.format("The parameter '%s' of value '%s' could not be converted to type '%s'", ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

        return ApiResponse.ApiError(HttpStatus.INTERNAL_SERVER_ERROR, message, ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex)  {
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        String message = "권한이 필요합니다.";

        return ApiResponse.ApiError(HttpStatus.INTERNAL_SERVER_ERROR, message, ex);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex)  {

        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        String message = "아이디 또는 비밀번호가 일치하지 않습니다.";


        return ApiResponse.ApiError(HttpStatus.INTERNAL_SERVER_ERROR, message, ex);
    }

    // TODO > 에러 발생지점 확인할 것
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex) {
        log.info("handleMaxUploadSizeExceededException", ex);

        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        String message = "업로드 가능한 파일 사이즈를 초과하였습니다. 10MB 이하의 파일을 업로드해주세요.";
        return ApiResponse.ApiError(HttpStatus.INTERNAL_SERVER_ERROR, message, ex);
    }

    /* ------------------------------------------------------------------------------
     * Jwt Error Handling
     * ------------------------------------------------------------------------------ */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Object> handleExpiredJwtException(ExpiredJwtException ex)  {
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        return ApiResponse.ApiError(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex);
    }


    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Object> handleJwtException(JwtException ex)  {
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        return ApiResponse.ApiError(BAD_REQUEST, ex.getMessage(), ex);
    }



    /* ------------------------------------------------------------------------------
     * FCM Error Handling
     * ------------------------------------------------------------------------------ */
    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<Object> handleExecutionException(ExecutionException ex)  {
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        return ApiResponse.ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex)  {
        // Setting Sentry Config
        CommonUtil.setSentryConfig(ex);

        return ApiResponse.ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
    }



}