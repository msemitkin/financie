package com.github.msemitkin.financie.telegram.api;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

@Component
public class TelegramApi {
    private final AbsSender absSender;

    public TelegramApi(AbsSender absSender) {
        this.absSender = absSender;
    }

    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) {
        try {
            return absSender.execute(method);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
