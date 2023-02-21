package com.github.msemitkin.financie.domain;

import java.util.List;

public class TransactionUtil {
    private TransactionUtil() {
    }

    public static double sum(List<Transaction> transactions) {
        return transactions.stream()
            .reduce(0.0, (total, transaction) -> total + transaction.amount(), Double::sum);
    }

}
