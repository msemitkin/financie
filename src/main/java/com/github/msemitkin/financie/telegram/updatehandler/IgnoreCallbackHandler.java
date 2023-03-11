package com.github.msemitkin.financie.telegram.updatehandler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Component
public class IgnoreCallbackHandler implements UpdateHandler {
    @Override
    public boolean canHandle(Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getData)
            .map("-1"::equals)
            .orElse(false);
    }

    @Override
    public void handleUpdate(Update update) {
        //do nothing
    }
}
