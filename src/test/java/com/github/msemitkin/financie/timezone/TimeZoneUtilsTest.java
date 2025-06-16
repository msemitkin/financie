package com.github.msemitkin.financie.timezone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;

class TimeZoneUtilsTest {

    @Test
    void startOfMonthForZoneIdIsConvertedToUtc() {
        ZoneId zoneId = ZoneId.of("Europe/Berlin");
        YearMonth yearMonth = YearMonth.of(2024, 2);

        LocalDateTime expected = yearMonth
            .atDay(1)
            .atStartOfDay(zoneId)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();

        LocalDateTime actual = TimeZoneUtils.getUTCStartOfTheMonthInTimeZone(yearMonth, zoneId);

        Assertions.assertEquals(expected, actual);
    }
}
