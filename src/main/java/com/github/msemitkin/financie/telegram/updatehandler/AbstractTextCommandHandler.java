package com.github.msemitkin.financie.telegram.updatehandler;

import org.springframework.lang.NonNull;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

public abstract class AbstractTextCommandHandler implements UpdateHandler {
    @NonNull
    private final String command;

    protected AbstractTextCommandHandler(@NonNull String command) {
        this.command = command;
    }

    @Override
    public final boolean canHandle(Update update) {
        return Optional.ofNullable(update.getMessage())
            .map(Message::getText)
            .map(command::equals)
            .orElse(false);
    }

}
