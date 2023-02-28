package com.github.msemitkin.financie.telegram.util;

import com.github.msemitkin.financie.domain.Transaction;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatDate;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;

public class TransactionUtil {
    private TransactionUtil() {
    }

    public static String getTransactionRepresentation(Transaction transaction) {
        return "%s : %s".formatted(formatDate(transaction.time().toLocalDate()), formatNumber(transaction.amount()));
    }
}
