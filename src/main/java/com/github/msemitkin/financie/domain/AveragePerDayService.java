package com.github.msemitkin.financie.domain;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
public class AveragePerDayService {

    public double getAveragePerDay(double total, @NonNull YearMonth yearMonth) {
        LocalDateTime startInclusive = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endExclusive = YearMonth.now().equals(yearMonth)
            ? LocalDateTime.now().plusDays(1)
            : yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        long numberOfDays = Duration.between(startInclusive, endExclusive).toDays();
        return total / numberOfDays;
    }
}
