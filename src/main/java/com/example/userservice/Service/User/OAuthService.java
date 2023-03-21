package com.example.userservice.Service.User;

import com.example.userservice.Common.CommonUtil.CommonUtil;
import com.example.userservice.Dto.Common.FileCategory;
import com.example.userservice.Dto.OAuth.OAuthProfile;
import com.example.userservice.Dto.OAuth.OAuthProfileFactory;
import com.example.userservice.Dto.OAuth.OAuthProvider;
import com.example.userservice.Dto.OAuth.OAuthRequestDTO;
import com.example.userservice.Dto.User.TokenDTO;
import com.example.userservice.Model.AppUser;
import com.example.userservice.Security.Filter.JwtProvider;
import com.example.userservice.Service.Common.CommonService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.example.userservice.Dto.OAuth.OAuthProvider.GOOGLE;
import static com.example.userservice.Dto.OAuth.OAuthProvider.KAKAO;


@RequiredArgsConstructor
@Log4j2
@Service
public class OAuthService {

    private final UserService userService;
    private final CommonService commonService;
    private final Environment env;
    private final JwtProvider jwtProvider;



    public Optional<TokenDTO> createToken(AppUser user){
        // Security Context Holder 저장
        // JWT Token 발급
        final String accessToken = jwtProvider.generateAccessToken(user.getEmail(),user.getAppUserRoles());
        final String refreshToken = jwtProvider.generateRefreshToken(user.getEmail(),null);

        TokenDTO tokenDTO = new TokenDTO();

        tokenDTO.setAccessToken(accessToken);
        tokenDTO.setRefreshToken(refreshToken);

        return Optional.ofNullable(tokenDTO);
    }

    public TokenDTO oAuthLogin(OAuthRequestDTO oAuthRequestDTO) throws IOException {

        // Step1. 유저 프로파일 불러오기
        OAuthProfile oAuthProfile = this.oAuthProfile(oAuthRequestDTO).orElseThrow(()->new RuntimeException("Invalid Credential"));
        String provider = oAuthRequestDTO.getProvider().getName();

        if(provider == null){
            throw new RuntimeException();
        }
        // Step2. 회원가입 (현재 oAuthSignUp 메소드 내부에서 존재하는 유저 여부를 체크 한다. 불필요하게 oAuthSignUp을 태우는건 아닌지 추후 확인필요)
        TokenDTO tokenDTO = this.oAuthSignUp(oAuthProfile,provider);

        return tokenDTO;
    }

    public TokenDTO oAuthSignUp(OAuthProfile oAuthProfile, String provider) throws IOException {

        AppUser appUser = new AppUser();
        appUser.setEmail(oAuthProfile.getEmail());      // 이메일 셋팅
        appUser.setUsername(oAuthProfile.getName());    // 이름 셋팅


        // Step1. 프로필 이미지 파일 받아오기
        MultipartFile multipartFile = commonService.downloadImageAsMultipartFile(oAuthProfile.getImageUrl());
        // Step2. S3 Bucket 에 업로드
        String url = commonService.uploadFile(FileCategory.PROFILE.getName(),multipartFile);
        // Step3. URL 경로 셋팅 및 프로바이더 셋팅
        appUser.setProfilePicture(url);
        // 로그인 제공자 셋팅
        appUser.setProvider(provider);

        // Step4. 토큰 발급
        final TokenDTO tokenDTO = createToken(appUser).orElseThrow(()->new JwtException("토큰이 생성되지 않았습니다."));
        // Step5. 토큰 셋팅
        appUser.setRefreshToken(tokenDTO.getRefreshToken());
        // Step6. 유저정보저장
        userService.socialSignUp(appUser);

        // Step7. token 리턴
        return tokenDTO;
    }

    public Optional<OAuthProfile> oAuthProfile(OAuthRequestDTO oauthRequestDTO) throws JsonProcessingException {

        HttpHeaders headers =  new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + oauthRequestDTO.getAccessToken());

        // Provider 존재 체크
        OAuthProvider provider = oauthRequestDTO.getProvider();

        // Optional requestUrl 정의
        String requestUrl = new String();
        if (provider.equals(KAKAO)) {
            requestUrl =env.getProperty("social.kakao.url.profile");
        } else if (provider.equals(GOOGLE)) {
            requestUrl = env.getProperty("social.google.url.profile");
        }

        if(requestUrl == null)
            throw new RuntimeException("소셜 프로파일을 가져올 requestUrl이 정의되지 않았습니다.");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        try {
            // HTTP 요청
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> attributes = mapper.readValue(response.getBody(), Map.class);
                return Optional.ofNullable(OAuthProfileFactory.getOAuthUserInfo(provider,attributes));
            }
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        }
        throw new RuntimeException("Invalid Credentials");
    }


}
