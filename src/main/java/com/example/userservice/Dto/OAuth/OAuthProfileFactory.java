package com.example.userservice.Dto.OAuth;

import com.example.userservice.Dto.OAuth.impl.GoogleOAuthProfile;
import com.example.userservice.Dto.OAuth.impl.KakaoOAuthProfile;

import java.util.Map;

public class OAuthProfileFactory {
    public static OAuthProfile getOAuthUserInfo(OAuthProvider providerType, Map<String, Object> attributes) {
        switch (providerType) {
            case GOOGLE: return new GoogleOAuthProfile(attributes);
            case KAKAO: return new KakaoOAuthProfile(attributes);
            default: throw new IllegalArgumentException("Invalid Provider Type.");
        }
    }
}