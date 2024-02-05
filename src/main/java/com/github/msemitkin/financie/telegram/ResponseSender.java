package com.github.msemitkin.financie.telegram;

import com.github.msemitkin.financie.telegram.api.TelegramApi;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
public class ResponseSender {
    private final TelegramApi telegramApi;

    public ResponseSender(TelegramApi telegramApi) {
        this.telegramApi = telegramApi;
    }

    public void sendResponse(
        long chatId,
        @Nullable Integer messageIdToReply,
        @Nullable InlineKeyboardMarkup keyboard,
        String message
    ) {
        sendResponse(chatId, messageIdToReply, keyboard, message, true);
    }

    public void sendResponse(
        long chatId,
        @Nullable Integer messageIdToReply,
        @Nullable InlineKeyboardMarkup keyboard,
        String message,
        boolean useMarkdownV2
    ) {
        if (messageIdToReply != null) {
            reply(chatId, messageIdToReply, keyboard, message, useMarkdownV2);
        } else {
            sendWithoutReply(chatId, keyboard, message, useMarkdownV2);
        }
    }

    private void reply(
        long chatId,
        int messageId,
        @Nullable InlineKeyboardMarkup keyboard,
        String message,
        boolean useMarkdownV2
    ) {
        telegramApi.execute(EditMessageText.builder()
            .chatId(chatId)
            .messageId(messageId)
            .text(message)
            .parseMode(useMarkdownV2 ? ParseMode.MARKDOWNV2 : null)
            .replyMarkup(keyboard)
            .build());
    }

    private void sendWithoutReply(
        long chatId,
        @Nullable InlineKeyboardMarkup keyboard,
        String message,
        boolean useMarkdownV2
    ) {
        telegramApi.execute(SendMessage.builder()
            .chatId(chatId)
            .text(message)
            .parseMode(useMarkdownV2 ? ParseMode.MARKDOWNV2 : null)
            .replyMarkup(keyboard)
            .build());
    }
}
