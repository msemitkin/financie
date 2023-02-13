package com.github.msemitkin.financie.telegram.updatehandler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandler {
    boolean canHandle(Update update);

    void handleUpdate(Update update);
}
