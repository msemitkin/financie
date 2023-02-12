package com.github.msemitkin.financie.telegram.transaction;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

@Component
public class TransactionRecognizer {
    public boolean hasTransactionFormat(String messageText) {
        String[] split = messageText.split(" ", 2);
        return split.length == 2 && NumberUtils.isParsable(split[0]);
    }
}
