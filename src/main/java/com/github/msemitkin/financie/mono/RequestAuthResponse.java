package com.github.msemitkin.financie.mono;

public class RequestAuthResponse {
    private String tokenRequestId;
    private String acceptUrl;

    public String getTokenRequestId() {
        return tokenRequestId;
    }

    public void setTokenRequestId(String tokenRequestId) {
        this.tokenRequestId = tokenRequestId;
    }

    public String getAcceptUrl() {
        return acceptUrl;
    }

    public void setAcceptUrl(String acceptUrl) {
        this.acceptUrl = acceptUrl;
    }
}
