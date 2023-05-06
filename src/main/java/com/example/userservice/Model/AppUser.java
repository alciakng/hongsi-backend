package com.example.userservice.Model;

import java.sql.Timestamp;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name="TU001_USER")
@Getter // Create getters and setter
@Setter
@NoArgsConstructor
@DynamicUpdate
public class AppUser {

    // 기본키를 생성하는 방법 @Id, @GeneratedValue 사용
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer uid;

    @Column
    private Integer famId;

    @Size(min = 1, max = 255, message = "Minimum username length: 1 characters")
    @Column(unique = true, nullable = false)
    private String email;

    @Size(min = 1, max = 255, message = "Minimum username length: 1 characters")
    @Column(nullable = false)
    private String username;

    @Size(min = 8, message = "Minimum password length: 8 characters")
    private String password;

    @Column(length = 100)
    private String channel;

    @Column(length = 10)
    private String role;

    @Column(length = 10)
    private String grade;

    @Column(length = 10)
    private String level;

    @Column(insertable = false,updatable = true)
    private Boolean leader;

    @Column
    private String bio;              // 자기소개

    @Column
    private Integer age;             // 나이

    @Column
    private String job;              // 직업

    @Column
    private String addr1;            // addr1 (주소)

    @Column
    private String addr2;            // addr1 (상세주소)

    @Column
    private String profilePicPath;  // 프로필 사진

    @Column(insertable = true,updatable = false)
    private String refreshToken;    // 리프레쉬 토큰

    @Column(insertable = true,updatable = false)
    private Timestamp createdAt;    // 생성일자

    @Column(insertable = false,updatable = true)
    private Timestamp updatedAt;    // 업데이트일자

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "TU002_USER_ROLE",
        joinColumns = @JoinColumn(name = "uid")
    )
    Set<AppUserRole> appUserRoles;

}
