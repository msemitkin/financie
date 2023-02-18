package com.github.msemitkin.financie.telegram.transaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TransactionRecognizerTest {
    private final TransactionRecognizer transactionRecognizer = new TransactionRecognizer();

    @ParameterizedTest
    @ValueSource(strings = {"-1", "-1.5"})
    void testNegativeNumbersAreIgnored(String amount) {
        boolean hasTransactionFormat = transactionRecognizer.hasTransactionFormat(amount + " valid category");

        Assertions.assertFalse(hasTransactionFormat);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.0"})
    void testZeroIsIgnored(String amount) {
        boolean hasTransactionFormat = transactionRecognizer.hasTransactionFormat(amount + " valid category");

        Assertions.assertFalse(hasTransactionFormat);
    }

    @ParameterizedTest
    @ValueSource(strings = {"+100", "+100.5"})
    void testAmountsWithUnaryPlusAreIgnored(String amount) {
        boolean hasTransactionFormat = transactionRecognizer.hasTransactionFormat(amount + " valid category");

        Assertions.assertFalse(hasTransactionFormat);
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "100.5"})
    void testPositiveNumbersAreAllowed(String amount) {
        boolean hasTransactionFormat = transactionRecognizer.hasTransactionFormat(amount + " valid category");

        Assertions.assertTrue(hasTransactionFormat);
    }
}