package com.github.msemitkin.financie.telegram.util;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class FormatterUtil {
    public static String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_DATE);
    }

    public static String formatMonth(Month month) {
        return month.getDisplayName(TextStyle.FULL, Locale.getDefault());
    }
}
