package com.github.msemitkin.financie.telegram.callback;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;
import java.util.UUID;

@Component
public class CallbackDataExtractor {
    private final CallbackService callbackService;

    public CallbackDataExtractor(CallbackService callbackService) {
        this.callbackService = callbackService;
    }

    @NonNull
    public <T> T getCallbackData(@NonNull Update update, Class<T> tClass) {
        return Optional.ofNullable(getCallbackId(update))
            .map(id -> callbackService.getCallback(id, tClass))
            .map(Callback::payload)
            .orElseThrow();
    }

    @Nullable
    private UUID getCallbackId(Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getData)
            .map(UUID::fromString)
            .orElse(null);
    }
}
