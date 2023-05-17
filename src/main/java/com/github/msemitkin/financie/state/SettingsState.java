package com.github.msemitkin.financie.state;

import com.github.msemitkin.financie.domain.Location;
import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.localizatitonapi.GetTimezoneByLocationPort;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.keyboard.KeyboardService;
import com.github.msemitkin.financie.telegram.util.UpdateUtil;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getLocation;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getMessage;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.hasLocation;

@Component
public class SettingsState implements State {
    private final UserService userService;
    private final StateService stateService;
    private final TelegramApi telegramApi;
    private final GetTimezoneByLocationPort getTimezoneByLocationPort;
    private final KeyboardService keyboardService;
    private final Set<String> backCommands = ResourceService.getValues("button.back");

    public SettingsState(
        UserService userService,
        StateService stateService,
        TelegramApi telegramApi,
        GetTimezoneByLocationPort getTimezoneByLocationPort,
        KeyboardService keyboardService
    ) {
        this.userService = userService;
        this.stateService = stateService;
        this.telegramApi = telegramApi;
        this.getTimezoneByLocationPort = getTimezoneByLocationPort;
        this.keyboardService = keyboardService;
    }

    @Override
    public void handle(Update update) {
        long senderTelegramId = UpdateUtil.getSenderTelegramId(update);
        User user = userService.getUserByTelegramId(senderTelegramId);
        Locale locale = UserContextHolder.getContext().locale();

        String incomingMessage = getMessage(update);
        if (incomingMessage != null && backCommands.contains(incomingMessage)) {
            var keyboard = keyboardService.getKeyboardForState(StateType.IDLE, locale);
            telegramApi.execute(SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(incomingMessage)
                .replyMarkup(keyboard)
                .build());
            stateService.setStateType(user.id(), StateType.IDLE);
        } else if (hasLocation(update)) {
            TimeZone timeZone = getNewTimeZone(update);
            if (timeZone != null) {
                userService.updateTimeZone(user.id(), timeZone);
                String message = StringSubstitutor.replace(
                    ResourceService.getValue("timezone-updated-message", locale),
                    Map.of("timezone", timeZone.getID()));
                var keyboard = keyboardService.getKeyboardForState(StateType.IDLE, locale);
                telegramApi.execute(SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text(message)
                    .replyMarkup(keyboard)
                    .build());
                stateService.setStateType(user.id(), StateType.IDLE);
            }
        } else {
            telegramApi.execute(SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(ResourceService.getValue("sorry-message", locale))
                .build());
        }
    }

    private TimeZone getNewTimeZone(Update update) {
        Location location = getLocation(update);
        return Optional.ofNullable(location)
            .map(getTimezoneByLocationPort::getTimezoneByLocation)
            .orElse(null);
    }
}
