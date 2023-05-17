package com.github.msemitkin.financie.telegram.updatehandler.matcher;

import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import org.springframework.lang.Nullable;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CallbackQueryUpdateMatcher implements UpdateMatcher {
    private final CallbackService callbackService;
    private final Set<CallbackType> queryTypes;

    public CallbackQueryUpdateMatcher(
        CallbackService callbackService,
        Set<CallbackType> queryTypes
    ) {
        this.callbackService = callbackService;
        this.queryTypes = queryTypes;
    }

    @Override
    public boolean match(Update update) {
        return Optional.ofNullable(getCallbackId(update))
            .map(callbackService::getCallbackType)
            .map(queryTypes::contains)
            .orElse(false);
    }

    @Nullable
    private UUID getCallbackId(Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getData)
            .map(UUID::fromString)
            .orElse(null);
    }
}
