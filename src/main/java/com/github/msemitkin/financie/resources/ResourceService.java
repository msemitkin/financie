package com.github.msemitkin.financie.resources;

import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourceService {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("texts");

    private ResourceService() {
    }

    @NonNull
    public static String getValue(String key, Locale locale) {
        return ResourceBundle.getBundle("texts", locale).getString(key);
    }

    @NonNull
    @Deprecated
    public static String getValue(String key) {
        return BUNDLE.getString(key);
    }

    @NonNull
    public static Set<String> getValues(String key) {
        Locale[] locales = Locale.getAvailableLocales();
        return Arrays.stream(locales)
            .map(locale -> ResourceBundle.getBundle("texts", locale))
            .filter(bundle -> bundle.containsKey(key))
            .map(bundle -> bundle.getString(key))
            .collect(Collectors.toSet());
    }
}
