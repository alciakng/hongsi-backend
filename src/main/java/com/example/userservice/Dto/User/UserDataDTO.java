package com.example.userservice.Dto.User;

import com.example.userservice.Model.AppUserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserDataDTO {

    private String username;
    private String email;
    private String password;
    private String bio;             // 자기소개
    private String profilePicture;  // 프로필 사진 경로
    private String provider;        // 공급자
    List<AppUserRole> appUserRoles;

}