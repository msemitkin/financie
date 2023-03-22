package com.github.msemitkin.financie.telegram.updatehandler.system;

import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import com.github.msemitkin.financie.telegram.keyboard.KeyboardService;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractTextCommandHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;

@Component
public class StartMessageHandler extends AbstractTextCommandHandler {
    private final TelegramApi telegramApi;
    private final KeyboardService keyboardService;

    public StartMessageHandler(
        TelegramApi telegramApi,
        KeyboardService keyboardService
    ) {
        super(BotCommand.START.getCommand());
        this.telegramApi = telegramApi;
        this.keyboardService = keyboardService;
    }

    @Override
    public void handleUpdate(Update update) {
        long chatId = update.getMessage().getChatId();
        sendWelcomeMessage(chatId);
    }

    private void sendWelcomeMessage(Long chatId) {
        Locale userLocale = UserContextHolder.getContext().locale();
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            //TODO customize message for new and existing users
            .text(ResourceService.getValue("welcome-message", userLocale))
            .replyMarkup(keyboardService.getDefaultReplyMarkup(userLocale))
            .build();
        telegramApi.execute(sendMessage);
    }

}