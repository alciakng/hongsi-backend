package com.example.userservice.Security.UserDetail;

import com.example.userservice.Model.AppUser;
import com.example.userservice.Repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        final AppUser appUser = userRepository.findByEmail(email);

        /* --------------------------------------------------------------------------------------
         * DaoAuthenticationProvider 에서 UserDetails 가 null일 경우 에러처리를 하므로, 따로 에러처리가 필요없다.
         * --------------------------------------------------------------------------------------
        if(appUser == null){
            throw new RuntimeException("사용자의 아이디 또는 비밀번호가 일치하지 않습니다.");
        }*/

        return new UserDetailsImpl(
                appUser.getUsername(),
                appUser.getPassword(),
                appUser.getEmail(),
                appUser.getUsername(),
                appUser.getAppUserRoles()
        );
    }

}