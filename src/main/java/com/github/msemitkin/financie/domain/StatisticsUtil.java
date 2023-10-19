package com.github.msemitkin.financie.domain;

import java.util.List;

public class StatisticsUtil {
    private StatisticsUtil() {
    }

    public static double sum(List<CategoryStatistics> statistics) {
        return statistics.stream()
                .mapToDouble(CategoryStatistics::amount)
                .sum();
    }
}
