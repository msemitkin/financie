package com.github.msemitkin.financie.telegram.transaction;

import com.github.msemitkin.financie.telegram.MessageException;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

@Component
public class TransactionCommandValidator {
    private static final int CATEGORY_NAME_MAX_LENGTH = 64;

    public void validateTransaction(String messageText) {
        String[] split = messageText.split(" ", 2);
        if (split.length != 2 || !NumberUtils.isParsable(split[0])) {
            throw new MessageException("""
                I don't understand you.
                To record transaction, send it in the following format: <amount> <category>
                Example: 500 food
                """);
        }
        if (split[1].length() > CATEGORY_NAME_MAX_LENGTH) {
            throw new MessageException("Category name is too long :(");
        }
    }
}
