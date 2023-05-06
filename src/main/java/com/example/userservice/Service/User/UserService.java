package com.example.userservice.Service.User;

import com.example.userservice.Common.CommonResponse.ApiResponse;
import com.example.userservice.Dto.User.TokenDTO;
import com.example.userservice.Dto.User.UserDataDTO;
import com.example.userservice.Dto.User.UserResponseDTO;
import com.example.userservice.Model.AppUser;
import com.example.userservice.Model.AppUserRole;
import com.example.userservice.Repository.UserRepository;
import com.example.userservice.Security.Filter.JwtProvider;
import com.example.userservice.Security.UserDetail.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ModelMapper modelMapper;

    public Optional<UserResponseDTO> signUp(AppUser appUser){

        if (!userRepository.existsByUsername(appUser.getUsername())) {
            // 패스워드 암복호화 적용해야되는지 확인
            appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
            // ROLE_CLIENT 기본설정
            appUser.setAppUserRoles(Collections.singleton(AppUserRole.ROLE_CLIENT));
            // 유저정보 저장
            userRepository.save(appUser);

            return Optional.of(modelMapper.map(appUser, UserResponseDTO.class));
        } else{
            throw new RuntimeException("이미 존재하는 유저입니다.");
        }
    }


    public AppUser socialSignUp(AppUser appUser) {
        if (!userRepository.existsByEmail(appUser.getEmail())) {

            appUser.setAppUserRoles(Collections.singleton(AppUserRole.ROLE_CLIENT));
            userRepository.save(appUser);
        }
        return appUser;
    }


    public Optional<UserResponseDTO> getProfile() {

        // Step1. 토큰추출
        String token = jwtProvider.resolveToken()
                                  .orElseThrow(()->new RuntimeException("토큰을 추출하는데 실패하였습니다. 네트워크 상태를 확인해주세요."));
        // Step2. userDetails
        //UserDetails userDetails = (UserDetails) jwtProvider.getAuthentication(token).getPrincipal();

        // Step2. email로 유저 탐색
        AppUser appUser = userRepository.findByEmail(jwtProvider.getSubject(token));

        // Step3. 유저가 없는 경우 Exception 발생 처리
        if(appUser == null){
            throw new RuntimeException("해당하는 유저를 찾을 수 없습니다. 네트워크 상태를 확인해주세요");
        }

        // Step4. 유저 프로파일 ResponseDTO로 변환하여 리턴
        return Optional.of(modelMapper.map(appUser,UserResponseDTO.class));
    }

    public UserResponseDTO saveProfile(UserDataDTO userDataDTO) {

        // Step1. 유저정보 매핑
        AppUser appUser = modelMapper.map(userDataDTO,AppUser.class);
        // Step2. 유저정보 변경 or 추가정보 저장
        userRepository.save(appUser);
        // Step3. 변경된 유저정보 리턴
        return modelMapper.map(userRepository.findByEmail(userDataDTO.getEmail()),UserResponseDTO.class);
    }

    public void delete(String username) {
        userRepository.deleteByUsername(username);
    }

    public ResponseEntity<Object> reIssueToken(){

        // refreshToken 추출
        String refreshToken = jwtProvider.resolveToken().orElseThrow(()->new JwtException("refresh Token이 존재하지 않습니다."));
        // Step1. refreshToken 유효성 체크 (만료체크 포함)
        Date expireTime = jwtProvider.isValidToken(refreshToken);

        // Step2. 토큰으로부터 인증정보 추출
        Authentication authentication = jwtProvider.getAuthentication(refreshToken);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Step2. accessToken 재발행
        String accessToken = jwtProvider.generateAccessToken(userDetails.getEmail(), (Set<AppUserRole>) Set.copyOf(authentication.getAuthorities()));
        // Step3. refreshToken 재발행 (만료시간은 기존건 그대로 사용)
        refreshToken = jwtProvider.generateRefreshToken(userDetails.getEmail(),expireTime);

        // Step4. refreshToken 저장
        this.updateRefreshToken(userDetails.getEmail(),refreshToken);

        // Access Token, Refresh Token 저장할 Token DTO
        TokenDTO tokenDTO = new TokenDTO();

        // token 셋팅
        tokenDTO.setAccessToken(accessToken);
        tokenDTO.setRefreshToken(refreshToken);

        // 응답할때는 헤더는 별도로 셋팅하지 않고 json 객체만 리턴한다.
        return ApiResponse.ApiSuccess(tokenDTO);
    }


    public void updateRefreshToken(String email, String refreshToken) {

        // Step1. 해당유저 탐색
        AppUser appUser = userRepository.findByEmail(email);

        if(appUser == null){
            throw new RuntimeException("해당 사용자를 찾을 수 없습니다.");
        }
        // Step2. refreshToken 셋팅
        appUser.setRefreshToken(refreshToken);
        userRepository.save(appUser);
    }
}