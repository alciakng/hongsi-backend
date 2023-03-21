package com.example.userservice.Security.Filter;


import com.example.userservice.Common.CommonResponse.ApiResponse;
import com.example.userservice.Dto.User.UserDataDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import io.sentry.Breadcrumb;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.sentry.protocol.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Date;
import java.util.Map;


import static com.example.userservice.Security.Constant.JwtConstants.AUTHORIZATION_HEADER;
import static com.example.userservice.Security.Constant.JwtConstants.BEARER_PREFIX;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Qualifier("handlerExceptionResolver")
    @Autowired
    private HandlerExceptionResolver resolver;

    /**
     * 토큰 인증 정보를 현재 쓰레드의 SecurityContext 에 저장하는 역할 수행
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // Step1. Path 추출 및 헤더 추출
        String servletPath = request.getServletPath();
        String authrizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (servletPath.equals("/users/signin") || servletPath.equals("/users/signup") || servletPath.startsWith("/oauth/login") || servletPath.startsWith("/h2-console")  || servletPath.startsWith("/actuator") ) {

            // Sentry BreadCrumbs 기록 (요청정보를 기록)
            Breadcrumb breadcrumb = new Breadcrumb();
            breadcrumb.setCategory("auth");
            // TODO parameter 기록할것 (inputStream은 한번 소비되면 얻을 수 없는 문제로 인해 뒤에서 에러남)
            breadcrumb.setMessage("path : user/signin, parameter : ");
            breadcrumb.setLevel(SentryLevel.INFO);
            Sentry.addBreadcrumb(breadcrumb);

            filterChain.doFilter(request, response);
        } else if (authrizationHeader == null || !authrizationHeader.startsWith(BEARER_PREFIX)) {
            // 토큰값이 없거나 정상적이지 않다면 400 오류
            ApiResponse.ApiError(response, HttpStatus.FORBIDDEN,"",new RuntimeException("인증이 정상적으로 이루어지지 않았습니다. 다시 로그인해주세요."));
        } else {

            try {
                // Step1. Request Header에서 Access Token 추출
                String jwt = jwtProvider.resolveToken().orElseThrow(() -> new JwtException("토큰을 추출하는데 실패하였습니다.(토큰 미존재)"));
                // Step3. 토큰 유효성 체크
                Date expireTime = jwtProvider.isValidToken(jwt);

                System.out.println("jwt 통과");
                // Step4. 토큰으로 인증 정보를 추출
                Authentication authentication = jwtProvider.getAuthentication(jwt);
                // Step5. SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);
            } catch(Exception e){
                resolver.resolveException(request, response, null, e);
            }


        }
    }



}