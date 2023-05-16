package com.github.msemitkin.financie.telegram.updatehandler.system;

import com.github.msemitkin.financie.domain.Location;
import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.localizatitonapi.GetTimezoneByLocationPort;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.updatehandler.UpdateHandler;
import com.github.msemitkin.financie.telegram.util.UpdateUtil;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

@Component
public class SetTimezoneHandler implements UpdateHandler {
    private final TelegramApi telegramApi;
    private final UserService userService;
    private final GetTimezoneByLocationPort getTimezoneByLocationPort;

    public SetTimezoneHandler(
        TelegramApi telegramApi,
        UserService userService,
        GetTimezoneByLocationPort getTimezoneByLocationPort
    ) {
        this.telegramApi = telegramApi;
        this.userService = userService;
        this.getTimezoneByLocationPort = getTimezoneByLocationPort;
    }

    @Override
    public boolean canHandle(Update update) {
        return Optional.ofNullable(update)
            .map(Update::getMessage)
            .map(Message::getLocation)
            .isPresent();
    }

    @Override
    public void handleUpdate(Update update) {
        long senderTelegramId = UpdateUtil.getSenderTelegramId(update);
        User user = userService.getUserByTelegramId(senderTelegramId);
        TimeZone timeZone = getNewTimeZone(update);

        if (timeZone != null) {
            userService.updateTimeZone(user.id(), timeZone);

            Locale locale = UserContextHolder.getContext().locale();
            String messageTemplate = ResourceService.getValue("timezone-updated-message", locale);
            String message = StringSubstitutor.replace(messageTemplate,
                Map.of("timezone", timeZone.getID()));

            telegramApi.execute(SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(message)
                .build());
        }
    }

    private TimeZone getNewTimeZone(Update update) {
        var location = Optional.ofNullable(update)
            .map(Update::getMessage)
            .map(Message::getLocation)
            .map(loc -> new Location(loc.getLatitude(), loc.getLongitude()))
            .orElse(null);

        return Optional.ofNullable(location)
            .map(getTimezoneByLocationPort::getTimezoneByLocation)
            .orElse(null);
    }
}
