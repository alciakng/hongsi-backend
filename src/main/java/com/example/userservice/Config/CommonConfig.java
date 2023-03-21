package com.example.userservice.Config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CommonConfig {


    /**
     * 비밀번호 암호화 및 확인 클래스
     * securityConfig 내부에 선언하면 순환참조의 문제로 인하여, CommonConfig에 선언하였음
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * 클래스간 Mapper 공통
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
