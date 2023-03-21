package com.example.userservice.Dto.OAuth;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthRequestDTO {
    private OAuthProvider provider;
    private String accessToken;

}