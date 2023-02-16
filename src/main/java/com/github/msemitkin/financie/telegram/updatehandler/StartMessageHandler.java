package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;
import java.util.Optional;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class StartMessageHandler implements UpdateHandler {
    private final TelegramApi telegramApi;

    public StartMessageHandler(TelegramApi telegramApi) {
        this.telegramApi = telegramApi;
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

    private void sendWelcomeMessage(Long chatId) {
        ReplyKeyboardMarkup markup = ReplyKeyboardMarkup.builder()
            .keyboardRow(new KeyboardRow(List.of(
                KeyboardButton.builder()
                    .text(BotCommand.MONTHLY_STATISTICS.getCommand())
                    .build()
            )))
            .resizeKeyboard(true)
            .build();
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text("Welcome")
            .replyMarkup(markup)
            .build();
        telegramApi.execute(sendMessage);
    }

}