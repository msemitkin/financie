package com.github.msemitkin.financie.telegram.util;

import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class FormatterUtil {
    private FormatterUtil() {
    }

    @NonNull
    public static String formatDate(@NonNull LocalDate date) {
        return date.format(DateTimeFormatter.ISO_DATE);
    }

    @NonNull
    public static String formatMonth(@NonNull Month month) {
        return month.getDisplayName(TextStyle.FULL, Locale.getDefault());
    }
}
