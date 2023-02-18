package com.github.msemitkin.financie.telegram.api;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

@Component
public class TelegramApi {
    private final DefaultAbsSender absSender;

    public TelegramApi(DefaultAbsSender absSender) {
        this.absSender = absSender;
    }

    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) {
        try {
            return absSender.execute(method);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public Message execute(SendDocument sendDocument) {
        try {
            return absSender.execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public java.io.File downloadFile(File file) {
        try {
            return absSender.downloadFile(file);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
