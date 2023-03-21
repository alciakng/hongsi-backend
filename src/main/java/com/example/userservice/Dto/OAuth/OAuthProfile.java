package com.example.userservice.Dto.OAuth;

import java.util.Map;


public abstract class OAuthProfile {
    protected Map<String, Object> attributes;

    public OAuthProfile(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract String getImageUrl();
}