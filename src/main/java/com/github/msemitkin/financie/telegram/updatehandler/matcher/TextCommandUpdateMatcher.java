package com.github.msemitkin.financie.telegram.updatehandler.matcher;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;
import java.util.Set;

class TextCommandUpdateMatcher implements UpdateMatcher {
    private final Set<String> commands;

    TextCommandUpdateMatcher(Set<String> commands) {
        this.commands = commands;
    }

    @Override
    public boolean match(Update update) {
        return Optional.ofNullable(update.getMessage())
            .map(Message::getText)
            .map(commands::contains)
            .orElse(false);
    }
}
