package com.example.userservice.Config.Enum;

public enum FcmMsgConfig {
    SOUND("default"),
    COLOR("#FFFF00");

    private String value;

    FcmMsgConfig(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}