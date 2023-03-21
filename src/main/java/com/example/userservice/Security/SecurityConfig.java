package com.example.userservice.Security;

import com.example.userservice.Security.Filter.CustomAuthenticationFilter;
import com.example.userservice.Security.Filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity //spring security 설정을 활성화시켜주는 어노테이션
@RequiredArgsConstructor // 의존성주입을 위한 자동 생성자 생성
public class SecurityConfig {


    // 인증 실패 또는 인증헤더가 전달받지 못했을때 핸들러
    private final AuthenticationEntryPoint authenticationEntryPoint;
    // 인가 실패 핸들러
    private final AccessDeniedHandler accessDeniedHandler;
    // 인증 성공 핸들러
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    // 인증 실패 핸들러
    private final AuthenticationFailureHandler authenticationFailureHandler;

    private final JwtFilter jwtFilter;

    private final AuthenticationConfiguration authenticationConfiguration;


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/h2-console/**","/favicon.ico");
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .authorizeHttpRequests((authorize) -> authorize
                      .requestMatchers("/users/signin","/users/signup","/error","/actuator").permitAll()
                      .anyRequest().authenticated()); // 다른 url은 인증 후에 접근가능

        http
                .exceptionHandling((exceptions) -> exceptions
                      .authenticationEntryPoint(authenticationEntryPoint)
                      .accessDeniedHandler(accessDeniedHandler));

        http
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        http // h2 데이터베이스 웹화면 깨짐 오류로 인해 추가
                .headers()
                .frameOptions()
                .disable();

       return http.build();
    }


    /**
     * 사용자 요청 정보로 UserPasswordAuthenticationToken 발급하는 필터
     * CustomAuthenticationFilter는 @Component 또는 @Bean으로 등록하면 순환 참조가 발생하므로, 아래와 같이 new 키워드로 객체를 생성하여 필터를 생성하고 @Bean으로 등록하였다.
     * (순환참조 문제란 A 클래스가 B 클래스의 Bean 을 주입받고, B 클래스가 A 클래스의 Bean 을 주입받는 상황처럼 서로 순환되어 참조할 경우 발생하는 문제를 의미한다.)
     */
    @Bean
    public CustomAuthenticationFilter authenticationFilter() throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationConfiguration.getAuthenticationManager());
        // 필터 URL 설정
        customAuthenticationFilter.setFilterProcessesUrl("/users/signin");
        // 인증 성공 핸들러
        customAuthenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        // 인증 실패 핸들러
        customAuthenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        // BeanFactory에 의해 모든 property가 설정되고 난 뒤 실행
        customAuthenticationFilter.afterPropertiesSet();
        return customAuthenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


}