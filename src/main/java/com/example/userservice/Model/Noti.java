package com.example.userservice.Model;

import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="TU004_DEVICE_FCM_MESSAGE")
@Getter // Create getters and setters
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Noti {

    // 기본키를 생성하는 방법 @Id, @GeneratedValue 사용
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String message;  // 메시지

    @Column
    private Timestamp sendAt;   // 발송일시

    @Column
    private String topic; // 토픽

    @ManyToOne
    @JoinColumn(name="did")
    private Device device;

}
