package com.github.msemitkin.financie.locale;

public enum LanguageCode {
    ENGLISH("en"),
    UKRAINIAN("uk");

    private final String code;

    LanguageCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
