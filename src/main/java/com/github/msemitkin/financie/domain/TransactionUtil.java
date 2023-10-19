package com.github.msemitkin.financie.domain;

import java.util.List;

public class TransactionUtil {
    private TransactionUtil() {
    }

    public static double sum(List<Transaction> transactions) {
        return transactions.stream()
                .mapToDouble(Transaction::amount)
                .sum();
    }

}
