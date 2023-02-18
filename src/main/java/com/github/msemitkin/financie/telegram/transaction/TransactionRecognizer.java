package com.github.msemitkin.financie.telegram.transaction;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class TransactionRecognizer {
    public boolean hasTransactionFormat(@NonNull String messageText) {
        String[] split = messageText.split(" ", 2);
        return split.length == 2
               && NumberUtils.isParsable(split[0])
               && Double.parseDouble(split[0]) > 0;
    }
}
