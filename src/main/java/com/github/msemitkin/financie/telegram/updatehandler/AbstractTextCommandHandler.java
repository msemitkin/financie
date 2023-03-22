package com.github.msemitkin.financie.telegram.updatehandler;

import org.springframework.lang.NonNull;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;
import java.util.Set;

public abstract class AbstractTextCommandHandler implements UpdateHandler {
    @NonNull
    private final Set<String> commands;

    protected AbstractTextCommandHandler(@NonNull Set<String> commands) {
        this.commands = commands;
    }

    protected AbstractTextCommandHandler(@NonNull String command) {
        this(Set.of(command));
    }

    @Override
    public final boolean canHandle(Update update) {
        return Optional.ofNullable(update.getMessage())
            .map(Message::getText)
            .map(commands::contains)
            .orElse(false);
    }

}
