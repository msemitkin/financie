package com.github.msemitkin.financie.telegram.auth;

import java.util.Locale;
import java.util.TimeZone;

public record UserContext(Locale locale, TimeZone timeZone) {
}
