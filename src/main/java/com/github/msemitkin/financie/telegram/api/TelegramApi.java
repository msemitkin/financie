package com.github.msemitkin.financie.telegram.api;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;

@Component
public class TelegramApi {
    private final TelegramClient telegramClient;

    public TelegramApi(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public <T extends Serializable, M extends BotApiMethod<T>> T execute(M method) {
        try {
            return telegramClient.execute(method);
        } catch (TelegramApiException e) {
            throw new TelegramApiMethodException(e);
        }
    }

    public Message execute(SendDocument sendDocument) {
        try {
            return telegramClient.execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new TelegramApiMethodException(e);
        }
    }

    public java.io.File downloadFile(File file) {
        try {
            return telegramClient.downloadFile(file);
        } catch (TelegramApiException e) {
            throw new TelegramApiMethodException(e);
        }
    }
}
