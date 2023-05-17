package com.github.msemitkin.financie.telegram.eventhandler;

import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserLanguageUpdatedEvent;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.state.StateService;
import com.github.msemitkin.financie.state.StateType;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.keyboard.KeyboardService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Locale;

@Component
public class UserLanguageUpdatedEventHandler {
    private final TelegramApi telegramApi;
    private final UserService userService;
    private final KeyboardService keyboardService;
    private final StateService stateService;

    public UserLanguageUpdatedEventHandler(
        TelegramApi telegramApi,
        UserService userService,
        KeyboardService keyboardService,
        StateService stateService
    ) {
        this.telegramApi = telegramApi;
        this.userService = userService;
        this.keyboardService = keyboardService;
        this.stateService = stateService;
    }

    @EventListener(UserLanguageUpdatedEvent.class)
    public void handleUserLanguageUpdatedEvent(UserLanguageUpdatedEvent event) {
        User user = userService.getUserById(event.getUserId());
        String languageCode = user.languageCode();

        Locale locale = new Locale(languageCode);

        StateType stateType = stateService.getStateType(user.id());
        telegramApi.execute(SendMessage.builder()
            .chatId(user.telegramChatId())
            .text(ResourceService.getValue("language-updated", locale))
            .replyMarkup(keyboardService.getKeyboardForState(stateType, locale))
            .build());
    }
}
