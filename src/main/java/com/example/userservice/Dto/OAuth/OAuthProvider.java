package com.example.userservice.Dto.OAuth;

public enum OAuthProvider {
    KAKAO, GOOGLE;

    public String getName() {
        return name();
    }

}