package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.telegram.callback.Callback;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractQueryHandler implements UpdateHandler {
    protected final CallbackService callbackService;
    private final Set<CallbackType> queryTypes;

    protected AbstractQueryHandler(
        CallbackService callbackService,
        Set<CallbackType> queryTypes
    ) {
        this.callbackService = callbackService;
        this.queryTypes = queryTypes;
    }

    protected AbstractQueryHandler(
        CallbackType queryType, CallbackService callbackService
    ) {
        this(callbackService, Set.of(queryType));
    }

    @Override
    public final boolean canHandle(Update update) {
        return Optional.ofNullable(getCallbackId(update))
            .map(callbackService::getCallbackType)
            .map(queryTypes::contains)
            .orElse(false);
    }

    @Nullable
    private UUID getCallbackId(Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getData)
            .map(string -> {
                try {
                    return UUID.fromString(string);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            })
            .orElse(null);
    }

    @NonNull
    protected <T> T getCallbackData(@NonNull Update update, Class<T> tClass) {
        return Optional.ofNullable(getCallbackId(update))
            .map(id -> callbackService.getCallback(id, tClass))
            .map(Callback::payload)
            .orElseThrow();
    }
}
