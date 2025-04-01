package com.github.msemitkin.financie.telegram.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationUrlProvider {
    private final String applicationRootUrl;

    public ApplicationUrlProvider(@Value("${application.root-url}") String applicationRootUrl) {
        this.applicationRootUrl = applicationRootUrl;
    }

    public String getApplicationRootUrl() {
        return applicationRootUrl;
    }

}
