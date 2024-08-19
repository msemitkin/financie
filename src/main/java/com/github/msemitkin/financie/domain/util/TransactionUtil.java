package com.github.msemitkin.financie.domain.util;

import com.github.msemitkin.financie.domain.Transaction;

import java.time.ZoneId;

public class TransactionUtil {
    private TransactionUtil() {
    }

    public static Transaction atZoneSameInstant(Transaction transaction, ZoneId zoneId) {
        return new Transaction(
            transaction.id(),
            transaction.userId(),
            transaction.amount(),
            transaction.category(),
            transaction.description(),
            transaction.time().atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId).toLocalDateTime()
        );
    }
}
