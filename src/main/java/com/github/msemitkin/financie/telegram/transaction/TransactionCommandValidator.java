package com.github.msemitkin.financie.telegram.transaction;

import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.MessageException;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

@Component
public class TransactionCommandValidator {
    private static final int CATEGORY_NAME_MAX_LENGTH = 64;
    private final String transactionInvalidMessage = ResourceService.getValue("exception.transaction-format-invalid");
    private final String categoryTooLongMessage = ResourceService.getValue("exception.category-too-long");

    public void validateTransaction(String messageText) {
        String[] split = messageText.split(" ", 2);
        if (split.length != 2 || !NumberUtils.isParsable(split[0])) {
            throw new MessageException(transactionInvalidMessage);
        }
        if (split[1].length() > CATEGORY_NAME_MAX_LENGTH) {
            throw new MessageException(categoryTooLongMessage);
        }
    }
}
