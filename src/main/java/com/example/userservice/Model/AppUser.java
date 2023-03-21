package com.example.userservice.Model;

import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="TU001")
@Getter // Create getters and setters
@Setter
@NoArgsConstructor
public class AppUser {

    // 기본키를 생성하는 방법 @Id, @GeneratedValue 사용
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(min = 1, max = 255, message = "Minimum username length: 1 characters")
    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Size(min = 8, message = "Minimum password length: 8 characters")
    private String password;

    @Column
    private String bio;             // 자기소개
    @Column
    private String profilePicture;  // 프로필 사진

    @Column(length = 100)
    private String provider;

    @Column
    private String refreshToken;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "TU002",
        joinColumns = @JoinColumn(name = "user_id")
    )
    Set<AppUserRole> appUserRoles;





}
