package com.github.msemitkin.financie.telegram.updatehandler.system;

import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.state.StateService;
import com.github.msemitkin.financie.state.StateType;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContext;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.updatehandler.BaseUpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import com.github.msemitkin.financie.telegram.util.UpdateUtil;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;
import java.util.Map;

import static com.github.msemitkin.financie.telegram.util.KeyboardUtil.requestLocationButton;
import static com.github.msemitkin.financie.telegram.util.KeyboardUtil.textButton;

@Component
public class SettingsHandler extends BaseUpdateHandler {
    private final UserService userService;
    private final StateService stateService;
    private final TelegramApi telegramApi;

    public SettingsHandler(
        UserService userService,
        StateService stateService,
        TelegramApi telegramApi) {
        super(UpdateMatcher.textCommandMatcher(ResourceService.getValues("button.settings")));
        this.userService = userService;
        this.stateService = stateService;
        this.telegramApi = telegramApi;
    }

    @Override
    public void handleUpdate(Update update) {
        long senderTelegramId = UpdateUtil.getSenderTelegramId(update);
        User user = userService.getUserByTelegramId(senderTelegramId);
        UserContext userContext = UserContextHolder.getContext();
        telegramApi.execute(SendMessage.builder()
            .chatId(update.getMessage().getChatId())
            .text(getMessage(userContext))
            .replyMarkup(getReplyMarkup(userContext))
            .build());
        stateService.setStateType(user.id(), StateType.SETTINGS);
    }

    private static ReplyKeyboardMarkup getReplyMarkup(UserContext userContext) {
        return ReplyKeyboardMarkup.builder()
            .keyboardRow(new KeyboardRow(List.of(
                requestLocationButton(ResourceService.getValue("button.change-timezone", userContext.locale()))
            )))
            .keyboardRow(new KeyboardRow(List.of(
                textButton(ResourceService.getValue("button.back", userContext.locale()))
            )))
            .resizeKeyboard(true)
            .build();
    }

    @NonNull
    private String getMessage(UserContext userContext) {
        String messageTemplate = ResourceService.getValue("message.settings", userContext.locale());
        return StringSubstitutor.replace(messageTemplate,
            Map.of("language", userContext.locale().getDisplayLanguage(userContext.locale()),
                "timezone", userContext.timeZone().getID()));
    }
}
