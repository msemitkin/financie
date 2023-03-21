package com.github.msemitkin.financie.locale;

import java.util.Arrays;

public class SupportedLanguageChecker {
    public static boolean isSupported(String languageCode) {
        return Arrays.stream(LanguageCode.values())
            .map(LanguageCode::getCode)
            .anyMatch(code -> code.equals(languageCode));
    }
}
