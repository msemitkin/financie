package com.github.msemitkin.financie.telegram.updatehandler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.lang.NonNull;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;
import java.util.Set;

public abstract class AbstractQueryHandler implements UpdateHandler {
    private final Set<String> queryTypes;

    protected AbstractQueryHandler(Set<String> queryTypes) {
        this.queryTypes = queryTypes;
    }

    protected AbstractQueryHandler(String queryType) {
        this(Set.of(queryType));
    }

    @Override
    public final boolean canHandle(Update update) {
        return getCallbackDataOpt(update)
            .map(json -> json.get("type"))
            .map(JsonElement::getAsString)
            .map(queryTypes::contains)
            .orElse(false);
    }

    @NonNull
    protected JsonObject getCallbackData(@NonNull Update update) {
        return getCallbackDataOpt(update).orElseThrow();
    }

    private Optional<JsonObject> getCallbackDataOpt(@NonNull Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getData)
            .map(data -> new Gson().fromJson(data, JsonObject.class));
    }
}
