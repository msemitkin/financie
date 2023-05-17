package com.github.msemitkin.financie.timezone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

public class TimeZoneUtils {
    private TimeZoneUtils() {
    }

    public static LocalDateTime getUTCStartOfTheDayInTimeZone(ZoneId zoneId) {
        return LocalDate.now(zoneId)
            .atStartOfDay()
            .atZone(zoneId)
            .withZoneSameInstant(ZoneId.of("UTC"))
            .toLocalDateTime();
    }

    public static LocalDateTime getUTCStartOfTheMonthInTimeZone(ZoneId zoneId) {
        return YearMonth.now(zoneId)
            .atDay(1)
            .atStartOfDay()
            .atZone(zoneId)
            .withZoneSameInstant(ZoneId.of("UTC"))
            .toLocalDateTime();
    }
}
