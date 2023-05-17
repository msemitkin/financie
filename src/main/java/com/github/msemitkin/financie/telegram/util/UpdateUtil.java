package com.github.msemitkin.financie.telegram.util;

import com.github.msemitkin.financie.domain.Location;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

public class UpdateUtil {
    private UpdateUtil() {
    }

    @Nullable
    public static String getMessage(Update update) {
        return Optional.ofNullable(update.getMessage())
            .map(Message::getText)
            .orElse(null);
    }

    public static Long getChatId(@NonNull Update update) {
        return Optional.ofNullable(update.getMessage())
            .map(Message::getChatId)
            .orElseGet(() -> Optional.ofNullable(update.getCallbackQuery())
                .map(CallbackQuery::getMessage)
                .map(Message::getChatId)
                .orElse(null));
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

    public static User getFrom(@NonNull Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getFrom)
            .orElseGet(() -> Optional
                .ofNullable(update.getMessage())
                .map(Message::getFrom)
                .orElse(null));
    }

    public static boolean hasLocation(Update update) {
        return Optional.ofNullable(update.getMessage())
            .map(Message::getLocation)
            .isPresent();
    }

    public static Location getLocation(Update update) {
        return Optional.ofNullable(update.getMessage())
            .map(Message::getLocation)
            .map(loc -> new Location(loc.getLatitude(), loc.getLongitude()))
            .orElse(null);
    }
}
