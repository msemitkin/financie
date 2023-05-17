package com.github.msemitkin.financie.domain;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TransactionValidator {

    public void validateTransaction(SaveTransactionCommand saveTransactionCommand) {
        if (saveTransactionCommand.amount() <= 0.0) {
            throw new TransactionValidationException("Amount cannot be negative");
        }
        if (saveTransactionCommand.category().length() > 64 || saveTransactionCommand.category().isEmpty()) {
            throw new TransactionValidationException("Category is invalid");
        }
        if (saveTransactionCommand.utcDateTime() != null
            && saveTransactionCommand.utcDateTime().isAfter(LocalDateTime.now())) {
            throw new TransactionValidationException("Transactions cannot be in future");
        }
    }
}
