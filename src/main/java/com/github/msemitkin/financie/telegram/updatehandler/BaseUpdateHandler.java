package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class BaseUpdateHandler implements UpdateHandler {
    private final UpdateMatcher updateMatcher;

    protected BaseUpdateHandler(UpdateMatcher updateMatcher) {
        this.updateMatcher = updateMatcher;
    }

    @Override
    public final boolean canHandle(Update update) {
        return updateMatcher.match(update);
    }

}
