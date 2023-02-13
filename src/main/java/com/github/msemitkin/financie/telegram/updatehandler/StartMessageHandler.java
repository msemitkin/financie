package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.telegram.command.BotCommand;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class StartMessageHandler implements UpdateHandler {
    private final AbsSender absSender;

    public StartMessageHandler(AbsSender absSender) {
        this.absSender = absSender;
    }

    @Override
    public boolean canHandle(Update update) {
        return Optional.ofNullable(update.getMessage())
            .map(Message::getText)
            .map(BotCommand.START.getCommand()::equals)
            .orElse(false);
    }

    @Override
    public void handleUpdate(Update update) {
        Long chatId = getChatId(update);
        sendWelcomeMessage(chatId);
    }

    private void sendWelcomeMessage(Long chatIt) {
        ReplyKeyboardMarkup markup = ReplyKeyboardMarkup.builder()
            .keyboardRow(new KeyboardRow(List.of(
                KeyboardButton.builder()
                    .text(BotCommand.MONTHLY_STATISTICS.getCommand())
                    .build()
            ))).build();
        sendMessage(chatIt, "Welcome", null, markup);
    }

    private void sendMessage(
        Long chatId,
        String text,
        @Nullable Integer replyToMessageId,
        @Nullable ReplyKeyboard replyKeyboard
    ) {
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .replyToMessageId(replyToMessageId)
            .replyMarkup(replyKeyboard)
            .build();
        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}