package com.github.msemitkin.financie.domain;

import java.util.List;

public class StatisticsUtil {
    private StatisticsUtil() {
    }

    public static double sum(List<CategoryStatistics> statistics) {
        return statistics.stream()
            .reduce(0.0, (result, stat) -> result + stat.amount(), Double::sum);
    }
}
