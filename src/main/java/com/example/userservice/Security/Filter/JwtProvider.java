package com.example.userservice.Security.Filter;


import com.example.userservice.Common.CommonResponse.ApiResponse;
import com.example.userservice.Model.AppUserRole;
import com.example.userservice.Service.User.UserService;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;


import static com.example.userservice.Security.Constant.JwtConstants.BEARER_PREFIX;
import static com.example.userservice.Security.Constant.JwtConstants.AUTHORIZATION_HEADER;

@Component
@RequiredArgsConstructor
public final class JwtProvider {

    private final UserDetailsService userDetailsService;

    // secret key
    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    // access token 유효시간
    @Value("${security.jwt.token.access-token-time}")
    private long accessTokenValidTime;

    // refresh token 유효시간
    @Value("${security.jwt.token.refresh-token-time}")
    private long refreshTokenValidTime;

    @PostConstruct
    private void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    /**
     * 토큰에서 Claim 추출
     */
    private Claims getClaimsFormToken(String token) throws JwtException{
        return Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(secretKey)).parseClaimsJws(token).getBody();
    }

    /**
     * 토큰에서 인증 Subject 추출
     */
    private String getSubject(String token) {
        return getClaimsFormToken(token).getSubject();
    }

    /**
     * 토큰에서 인증 정보 추출
     */
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getSubject(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * 엑세스 토큰 발급
     */
    public String generateAccessToken(String email, Set<AppUserRole> roles) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(email));
        claims.put("roles", roles);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * 리프레쉬 토큰 발급
     */
    public String generateRefreshToken(String email, Date expireTime) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(email));
        Date now = new Date();

        // expireTime 이 매겨변수로 넘어오지 않으면 신규생성
        // expireTime 이 매겨변수로 넘어오면 기존 expireTime 기반으로 토큰만 변경
        if(expireTime ==null){
            expireTime =new Date(now.getTime() + refreshTokenValidTime);
        }

        // 리프레쉬 토큰 발급
        String refreshToken = Jwts.builder()
                                  .setClaims(claims)
                                  .setIssuedAt(now)
                                  .setExpiration(expireTime)
                                  .signWith(SignatureAlgorithm.HS256, secretKey)
                                  .compact();
        return refreshToken;
    }


    /**
     * 토큰 검증
     */
    public Date isValidToken(String token) {
        try {
            Claims claims = getClaimsFormToken(token);
            // 아래 Exception으로 처리
            //!claims.getExpiration().before(new Date())

            // 만료시간 티런
            return claims.getExpiration();
        } catch (ExpiredJwtException e){
            throw e;
        } catch (JwtException | NullPointerException e) {
            throw e;
        }
    }


    /**
     * Request Header에서 토큰 추출
     */
    public Optional<String> resolveToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            String token = bearerToken.substring(7);

            if(token.isEmpty()){
                return Optional.empty();
            }else {
                return Optional.of(token);
            }
        }
        return Optional.empty();
    }

}