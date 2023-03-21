package com.example.userservice.Security.Handler;

import com.example.userservice.Common.CommonResponse.ApiResponse;
import com.example.userservice.Common.CommonUtil.CommonUtil;
import com.example.userservice.Dto.User.TokenDTO;
import com.example.userservice.Model.AppUserRole;
import com.example.userservice.Security.Filter.JwtProvider;
import com.example.userservice.Security.UserDetail.UserDetailsImpl;
import com.example.userservice.Service.User.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 전달받은 인증정보 SecurityContextHolder 에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // JWT Token 발급
        final String accessToken = jwtProvider.generateAccessToken(userDetails.getEmail(), (Set<AppUserRole>) Set.copyOf( authentication.getAuthorities()));
        final String refreshToken = jwtProvider.generateRefreshToken(userDetails.getEmail(),null);

        // refresh Token 저장
        userService.updateRefreshToken(userDetails.getEmail(),refreshToken);

        // Access Token, Refresh Token 저장할 Token DTO
        TokenDTO tokenDTO = new TokenDTO();

        tokenDTO.setAccessToken(accessToken);
        tokenDTO.setRefreshToken(refreshToken);

        // 응답할때는 헤더는 별도로 셋팅하지 않고 json 객체만 리턴한다.
        ApiResponse.ApiSuccess(response,tokenDTO);
    }


}