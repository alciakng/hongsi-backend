package com.example.userservice.Model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="TU003_USER_DEVICE")
@Getter // Create getters and setters
@Setter
@NoArgsConstructor
public class Device {

    // 기본키를 생성하는 방법 @Id, @GeneratedValue 사용
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String type;  // device type
    @Column
    private String topic;   // 주제
    @Column(nullable = false)
    private String token;   // 토큰

    @ManyToOne
    @JoinColumn(name="uid")
    private AppUser user;

}