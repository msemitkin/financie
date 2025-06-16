package com.github.msemitkin.financie.timezone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.stream.Stream;

class TimeZoneUtilsTest {

    static Stream<Arguments> monthAndExpectedUtc() {
        return Stream.of(
            org.junit.jupiter.params.provider.Arguments.of(
                YearMonth.of(2024, 2), // Before DST
                LocalDateTime.of(2024, 1, 31, 23, 0)
            ),
            org.junit.jupiter.params.provider.Arguments.of(
                YearMonth.of(2024, 7), // During DST
                LocalDateTime.of(2024, 6, 30, 22, 0)
            )
        );
    }

    @ParameterizedTest
    @MethodSource("monthAndExpectedUtc")
    void getUTCStartOfTheMonthInTimeZone_shouldBeConvertedToUtc_regardlessOfDst(YearMonth yearMonth, LocalDateTime expected) {
        ZoneId zoneId = ZoneId.of("Europe/Berlin");

        LocalDateTime actual = TimeZoneUtils.getUTCStartOfTheMonthInTimeZone(yearMonth, zoneId);

        Assertions.assertEquals(expected, actual);
    }
}
