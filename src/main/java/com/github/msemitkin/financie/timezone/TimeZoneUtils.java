package com.github.msemitkin.financie.timezone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

public class TimeZoneUtils {
    private TimeZoneUtils() {
    }

    public static LocalDateTime getUTCStartOfTheDayInTimeZone(ZoneId zoneId) {
        return getUTCStartOfTheDayInTimeZone(LocalDate.now(zoneId), zoneId);
    }

    public static LocalDateTime getUTCStartOfTheDayInTimeZone(LocalDate date, ZoneId zoneId) {
        return date.atStartOfDay()
            .atZone(zoneId)
            .withZoneSameInstant(ZoneId.of("UTC"))
            .toLocalDateTime();
    }

    public static LocalDateTime getUTCStartOfTheMonthInTimeZone(ZoneId zoneId) {
        return getUTCStartOfTheMonthInTimeZone(YearMonth.now(zoneId), zoneId);
    }

    public static LocalDateTime getUTCStartOfTheMonthInTimeZone(YearMonth yearMonth, ZoneId zoneId) {
        return yearMonth.atDay(1)
            .atStartOfDay()
            .atZone(zoneId)
            .withZoneSameInstant(ZoneId.of("UTC"))
            .toLocalDateTime();
    }
}
