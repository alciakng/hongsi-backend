package com.example.userservice.Common.CommonResponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ApiResponse<T> {

    private HttpStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)     // LocalDateTime ObjectMapper() 이용하여 직렬화 시 오류발생 해결
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // LocalDateTime ObjectMapper() 이용하여 역직렬화 시 오류발생 해결
    private LocalDateTime timestamp;
    private String message;
    private T data; // 성공 응답인 경우 리턴하는 data
    private String debugMessage;
    private List<ApiSubError> subErrors;

    private ApiResponse() {
        timestamp = LocalDateTime.now();
    }

    /**
     * Success response with data, message
     * @param status status
     * @param message message
     * @param data data
     */
    private ApiResponse(HttpStatus status, String message, T data) {
        this();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * Error Response with message
     * @param status error status
     * @param ex error thrown
     */
    private ApiResponse(HttpStatus status, String message, Throwable ex) {
        this();
        this.status = status;
        this.message = message;
        this.debugMessage = ex.getLocalizedMessage();
    }



    private void addSubError(ApiSubError subError) {
        if (subErrors == null) {
            subErrors = new ArrayList<>();
        }
        subErrors.add(subError);
    }

    private void addValidationError(String object, String field, Object rejectedValue, String message) {
        addSubError(new ApiValidationError(object, field, rejectedValue, message));
    }

    private void addValidationError(String object, String message) {
        addSubError(new ApiValidationError(object, message));
    }

    private void addValidationError(FieldError fieldError) {
        this.addValidationError(
                fieldError.getObjectName(),
                fieldError.getField(),
                fieldError.getRejectedValue(),
                fieldError.getDefaultMessage());
    }

    private void addValidationError(ObjectError objectError) {
        this.addValidationError(
                objectError.getObjectName(),
                objectError.getDefaultMessage());
    }

    /**
     * Utility method for adding error of ConstraintViolation. Usually when a @Validated validation fails.
     *
     * @param cv the ConstraintViolation
     */
    private void addValidationError(ConstraintViolation<?> cv) {
        this.addValidationError(
                cv.getRootBeanClass().getSimpleName(),
                ((PathImpl) cv.getPropertyPath()).getLeafNode().asString(),
                cv.getInvalidValue(),
                cv.getMessage());
    }


    /**
     * success ResponseEntity response with no message
     * @param data data
     */
    public static <T> ResponseEntity<Object> ApiSuccess(T data)  {
        ApiResponse apiResponse = new ApiResponse<T>(HttpStatus.OK, null ,data);
        return new ResponseEntity<>(apiResponse,apiResponse.getStatus());
    }

    public static <T> void ApiSuccess(HttpServletResponse httpServletResponse, T data) throws IOException {
        ApiResponse apiResponse = new ApiResponse<T>(HttpStatus.OK,null,data);

        ObjectMapper mapper = new ObjectMapper();
        httpServletResponse.setStatus(HttpStatus.OK.value());
        httpServletResponse.getWriter().write(mapper.writeValueAsString(apiResponse));
        httpServletResponse.flushBuffer();;
    }


    /**
     * success ResponseEntity response
     * @param message message
     * @param data data
     */
    public static <T> ResponseEntity<Object> ApiSuccess(String message, T data)  {
        ApiResponse apiResponse = new ApiResponse<T>(HttpStatus.OK,message,data);
        return new ResponseEntity<>(apiResponse,apiResponse.getStatus());
    }

    public static ResponseEntity<Object> ApiError(HttpStatus status, String message, Throwable ex){
        ApiResponse apiResponse = new ApiResponse<>(status, message, ex);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }


    public static void ApiError(HttpServletResponse httpServletResponse,HttpStatus status, String message, Throwable ex) throws IOException {
        ApiResponse apiResponse = new ApiResponse<>(status, message, ex);

        ObjectMapper mapper = new ObjectMapper();
        httpServletResponse.setStatus(status.value());
        httpServletResponse.getWriter().write(mapper.writeValueAsString(apiResponse));
        httpServletResponse.flushBuffer();;
    }

    public static <T> ResponseEntity<Object> ApiErrorWithSubError(HttpStatus status, String message, Throwable ex, List<T> errors){
        ApiResponse apiResponse = new ApiResponse<>(status, message, ex);

        errors.forEach( error -> {
            if (error instanceof FieldError) {
                apiResponse.addValidationError((FieldError) error);
            }else if (error instanceof ObjectError) {
                apiResponse.addValidationError((ObjectError) error);
            }
        });

        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }

    public static  ResponseEntity<Object> ApiErrorWithSubError(HttpStatus status, String message, Throwable ex, Set<ConstraintViolation<?>> constraintViolations){
        ApiResponse apiResponse = new ApiResponse<>(status, message, ex);
        constraintViolations.forEach(apiResponse::addValidationError);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }
}
