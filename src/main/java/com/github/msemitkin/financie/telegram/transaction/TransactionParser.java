package com.github.msemitkin.financie.telegram.transaction;


import org.springframework.stereotype.Component;

@Component
public class TransactionParser {

    public IncomingTransaction parseTransaction(String messageText) {
        double amount = parseAmount(messageText);
        String category = parseCategory(messageText);
        return new IncomingTransaction(amount, category);
    }

    private double parseAmount(String messageText) {
        return Double.parseDouble(messageText.substring(0, messageText.indexOf(" ")));
    }

    private String parseCategory(String messageText) {
        return messageText.substring(messageText.indexOf(" ") + 1);
    }
}
