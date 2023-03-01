package com.github.msemitkin.financie.telegram.util;

import org.springframework.lang.NonNull;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

public class UpdateUtil {
    private UpdateUtil() {
    }

    public static long getChatId(@NonNull Update update) {
        return Optional.ofNullable(update.getMessage())
            .map(Message::getChatId)
            .orElseGet(() -> Optional.ofNullable(update.getCallbackQuery())
                .map(CallbackQuery::getMessage)
                .map(Message::getChatId)
                .orElseThrow());
    }

    public static long getSenderTelegramId(@NonNull Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getFrom)
            .map(User::getId)
            .orElseGet(() -> Optional
                .ofNullable(update.getMessage())
                .map(Message::getFrom)
                .map(User::getId)
                .orElseThrow());
    }
}
