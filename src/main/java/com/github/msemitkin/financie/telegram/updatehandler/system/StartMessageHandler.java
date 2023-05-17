package com.github.msemitkin.financie.telegram.updatehandler.system;

import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.state.StateService;
import com.github.msemitkin.financie.state.StateType;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import com.github.msemitkin.financie.telegram.keyboard.KeyboardService;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import com.github.msemitkin.financie.telegram.util.UpdateUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;

@Component
public class StartMessageHandler {
    private static final StateType INITIAL_STATE_TYPE = StateType.IDLE;

    private final UpdateMatcher updateMatcher;
    private final TelegramApi telegramApi;
    private final KeyboardService keyboardService;
    private final StateService stateService;
    private final UserService userService;

    public StartMessageHandler(
        TelegramApi telegramApi,
        KeyboardService keyboardService,
        StateService stateService,
        UserService userService
    ) {
        this.stateService = stateService;
        this.userService = userService;
        this.updateMatcher = UpdateMatcher.textCommandMatcher(BotCommand.START.getCommand());
        this.telegramApi = telegramApi;
        this.keyboardService = keyboardService;
    }

    public boolean canHandle(Update update) {
        return updateMatcher.match(update);
    }

    public void handleUpdate(Update update) {
        long chatId = update.getMessage().getChatId();
        sendWelcomeMessage(chatId);
        long senderTelegramId = UpdateUtil.getSenderTelegramId(update);
        User user = userService.getUserByTelegramId(senderTelegramId);
        stateService.setStateType(user.id(), INITIAL_STATE_TYPE);
    }

    private void sendWelcomeMessage(Long chatId) {
        Locale userLocale = UserContextHolder.getContext().locale();
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            //TODO customize message for new and existing users
            .text(ResourceService.getValue("welcome-message", userLocale))
            .replyMarkup(keyboardService.getKeyboardForState(INITIAL_STATE_TYPE, userLocale))
            .build();
        telegramApi.execute(sendMessage);
    }

}
