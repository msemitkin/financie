package com.github.msemitkin.financie.mono;

import org.springframework.stereotype.Service;

@Service
public class MonobankService {
    private final MonobankIntegration monobankIntegration;

    public MonobankService(MonobankIntegration monobankIntegration) {
        this.monobankIntegration = monobankIntegration;
    }

    public RequestAuthResponse requestAuthUrl(String callbackUrl) {
        return monobankIntegration.requestAuth(callbackUrl);
    }

    public void registerWebHook(String requestId, String userWebHookUrl) {
        monobankIntegration.registerWebHook(requestId, userWebHookUrl);
    }
}
