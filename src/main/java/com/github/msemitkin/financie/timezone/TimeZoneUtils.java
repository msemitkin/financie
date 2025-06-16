package com.github.msemitkin.financie.timezone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;

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
        OffsetDateTime startOfTheMonth = OffsetDateTime.now(zoneId)
            .with(TemporalAdjusters.firstDayOfMonth())
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        return LocalDateTime.ofInstant(startOfTheMonth.toInstant(), ZoneId.of("UTC"));
    }

    public static LocalDateTime getUTCStartOfTheMonthInTimeZone(YearMonth yearMonth, ZoneId zoneId) {
        return yearMonth
            .atDay(1)
            .atStartOfDay(zoneId)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();
    }
}
