package com.example.userservice.Dto.OAuth.impl;

import com.example.userservice.Dto.OAuth.OAuthProfile;

import java.util.Map;

public class KakaoOAuthProfile extends OAuthProfile {

    public KakaoOAuthProfile(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

        if (properties == null) {
            return null;
        }

        return (String) properties.get("nickname");
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");

        if (kakao_account == null) {
            return null;
        }

        return (String) kakao_account.get("email");
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

        if (properties == null) {
            return null;
        }

        return (String) properties.get("thumbnail_image");
    }
}