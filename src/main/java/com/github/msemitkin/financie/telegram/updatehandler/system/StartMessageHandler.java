package com.github.msemitkin.financie.telegram.updatehandler.system;

import com.github.msemitkin.financie.domain.SaveOrUpdateUserCommand;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractTextCommandHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Component
public class StartMessageHandler extends AbstractTextCommandHandler {
    private final TelegramApi telegramApi;
    private final UserService userService;

    public StartMessageHandler(
        TelegramApi telegramApi,
        UserService userService
    ) {
        super(BotCommand.START.getCommand());
        this.telegramApi = telegramApi;
        this.userService = userService;
    }

    @Override
    public void handleUpdate(Update update) {
        long chatId = update.getMessage().getChatId();
        User user = update.getMessage().getFrom();
        var saveCommand = new SaveOrUpdateUserCommand(
            user.getId(),
            chatId,
            user.getFirstName(),
            user.getLastName(),
            user.getUserName()
        );
        userService.saveOrUpdateUser(saveCommand);
        sendWelcomeMessage(chatId);
    }

    private void sendWelcomeMessage(Long chatId) {
        ReplyKeyboardMarkup markup = ReplyKeyboardMarkup.builder()
            .keyboardRow(new KeyboardRow(List.of(
                KeyboardButton.builder().text(BotCommand.TODAY.getCommand()).build(),
                KeyboardButton.builder().text(BotCommand.MONTHLY_STATISTICS.getCommand()).build()
            )))
            .resizeKeyboard(true)
            .build();
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            //TODO customize message for new and existing users
            .text("""
                We're so happy to have you on board with Financie!
                If you have any questions or feedback, please don't hesitate to reach out. \
                Our friendly team is always here to help! \uD83D\uDE0A
                """)
            .replyMarkup(markup)
            .build();
        telegramApi.execute(sendMessage);
    }

}