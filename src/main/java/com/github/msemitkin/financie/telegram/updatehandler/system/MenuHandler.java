package com.github.msemitkin.financie.telegram.updatehandler.system;

import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.state.StateService;
import com.github.msemitkin.financie.state.StateType;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.keyboard.KeyboardService;
import com.github.msemitkin.financie.telegram.updatehandler.BaseUpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import com.github.msemitkin.financie.telegram.util.UpdateUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getMessage;
import static java.util.Objects.requireNonNull;

@Component
public class MenuHandler extends BaseUpdateHandler {
    private final UserService userService;
    private final StateService stateService;
    private final TelegramApi telegramApi;
    private final KeyboardService keyboardService;

    public MenuHandler(
        UserService userService,
        StateService stateService,
        TelegramApi telegramApi,
        KeyboardService keyboardService
    ) {
        super(UpdateMatcher.textCommandMatcher(ResourceService.getValues("button.menu")));
        this.userService = userService;
        this.stateService = stateService;
        this.telegramApi = telegramApi;
        this.keyboardService = keyboardService;
    }

    @Override
    protected void handleUpdate(Update update) {
        long senderTelegramId = UpdateUtil.getSenderTelegramId(update);
        User user = userService.getUserByTelegramId(senderTelegramId);
        Locale locale = UserContextHolder.getContext().locale();
        StateType nextState = StateType.MENU;

        telegramApi.execute(SendMessage.builder()
            .chatId(update.getMessage().getChatId())
            .text(requireNonNull(getMessage(update)))
            .replyMarkup(keyboardService.getKeyboardForState(nextState, locale))
            .build());
        stateService.setStateType(user.id(), nextState);
    }
}
