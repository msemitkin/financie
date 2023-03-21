package com.github.msemitkin.financie.telegram.updatehandler.system;

import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractTextCommandHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;
import java.util.Locale;

@Component
public class StartMessageHandler extends AbstractTextCommandHandler {
    private final TelegramApi telegramApi;

    public StartMessageHandler(TelegramApi telegramApi) {
        super(BotCommand.START.getCommand());
        this.telegramApi = telegramApi;
    }

    @Override
    public void handleUpdate(Update update) {
        long chatId = update.getMessage().getChatId();
        sendWelcomeMessage(chatId);
    }

    private void sendWelcomeMessage(Long chatId) {
        Locale userLocale = UserContextHolder.getContext().locale();
        ReplyKeyboardMarkup markup = ReplyKeyboardMarkup.builder()
            .keyboardRow(new KeyboardRow(List.of(
                KeyboardButton.builder().text(ResourceService.getValue("button.today", userLocale)).build(),
                KeyboardButton.builder().text(ResourceService.getValue("button.this-month", userLocale)).build()
            )))
            .resizeKeyboard(true)
            .build();
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            //TODO customize message for new and existing users
            .text(ResourceService.getValue("welcome-message", userLocale))
            .replyMarkup(markup)
            .build();
        telegramApi.execute(sendMessage);
    }

}