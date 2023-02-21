package com.github.msemitkin.financie.persistence.mapper;

import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.persistence.entity.TransactionEntity;

public class TransactionMapper {
    private TransactionMapper() {
    }

    public static Transaction toTransaction(TransactionEntity tran, CategoryNameSource categoryNameSource) {
        return new Transaction(
            tran.getId(),
            tran.getUserId(),
            tran.getAmount(),
            categoryNameSource.getName(tran.getCategoryId()),
            tran.getDescription(),
            tran.getDateTime()
        );
    }

    public static Transaction toTransaction(TransactionEntity tran, String category) {
        return toTransaction(tran, id -> category);
    }
}
