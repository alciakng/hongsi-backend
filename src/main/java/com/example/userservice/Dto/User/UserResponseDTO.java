package com.example.userservice.Dto.User;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class UserResponseDTO {

    private Integer uid;
    private Integer famId;
    private String email;
    private String username;
    private String channel;
    private String role;
    private String grade;
    private String level;
    private Boolean leader;
    private String bio;              // 자기소개
    private Integer age;             // 나이
    private String job;              // 직업
    private String addr1;            // addr1 (주소)
    private String addr2;            // addr1 (상세주소)
    private String profilePicPath;  // 프로필 사진
    private String refreshToken;    // 리프레쉬 토큰
    private Timestamp createdAt;    // 생성일자
    private Timestamp updatedAt;    // 업데이트일자
    List<String> appUserRoles;
}