package com.github.msemitkin.financie.domain;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

@Service
public class AveragePerDayService {

    public double getAveragePerDay(double total, @NonNull YearMonth zonedYearMonth, ZoneId zoneId) {
        LocalDateTime startInclusive = zonedYearMonth.atDay(1)
            .atStartOfDay()
            .atZone(zoneId)
            .withZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime();

        LocalDateTime endExclusive = YearMonth.now(zoneId).equals(zonedYearMonth)
            ? LocalDateTime.now().plusDays(1)
            : zonedYearMonth.plusMonths(1)
            .atDay(1)
            .atStartOfDay()
            .atZone(zoneId)
            .withZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime();

        long numberOfDays = Duration.between(startInclusive, endExclusive).toDays();
        return total / numberOfDays;
    }
}
