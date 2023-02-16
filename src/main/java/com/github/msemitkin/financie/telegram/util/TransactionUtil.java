package com.github.msemitkin.financie.telegram.util;

import com.github.msemitkin.financie.domain.Transaction;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatDate;

public class TransactionUtil {
    private TransactionUtil() {
    }

    public static String getTransactionRepresentation(Transaction transaction) {
        return "%s : %.1f".formatted(formatDate(transaction.time().toLocalDate()), transaction.amount());
    }
}
