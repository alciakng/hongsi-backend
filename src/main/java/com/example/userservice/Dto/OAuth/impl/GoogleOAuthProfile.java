package com.example.userservice.Dto.OAuth.impl;

import com.example.userservice.Dto.OAuth.OAuthProfile;

import java.util.Map;

public class GoogleOAuthProfile extends OAuthProfile {

    public GoogleOAuthProfile(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }
}
