package com.example.userservice.Dto.User;

import com.example.userservice.Model.AppUserRole;
import lombok.Data;

import java.util.List;

@Data
public class UserResponseDTO {

    private Integer id;
    private String username;
    private String email;
    private String bio;            // 자기소개
    private String profilePicture; // 프로필 사진경로
    private String provider;       // 공급자
    List<AppUserRole> appUserRoles;

}