package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.locale.SupportedLanguageChecker;
import com.github.msemitkin.financie.telegram.UpdateReceivedEvent;
import com.github.msemitkin.financie.telegram.auth.UserContext;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.transaction.UserMapper;
import com.github.msemitkin.financie.telegram.updatehandler.chain.UpdateHandlerChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getFrom;

@Component
public class UpdateListener {
    private static final Logger logger = LoggerFactory.getLogger(UpdateListener.class);

    private final UpdateHandlerChain updateHandlerChain;
    private final UserService userService;

    public UpdateListener(UpdateHandlerChain updateHandlerChain, UserService userService) {
        this.updateHandlerChain = updateHandlerChain;
        this.userService = userService;
    }

    @EventListener(UpdateReceivedEvent.class)
    public void onUpdateReceived(UpdateReceivedEvent event) {
        try {
            Update update = event.getUpdate();
            var user = updateUser(update);
            Locale userLocale = getUserLocale(user);
            TimeZone timeZone = Optional.ofNullable(user.timeZoneId())
                .map(TimeZone::getTimeZone)
                .orElse(TimeZone.getDefault());
            UserContextHolder.setContext(new UserContext(userLocale, timeZone));
            processEvent(event);
        } catch (Exception e) {
            logger.error("Unhandled exception", e);
        } finally {
            UserContextHolder.clearContext();
        }
    }

    private Locale getUserLocale(com.github.msemitkin.financie.domain.User user) {
        return Optional.of(user)
            .map(com.github.msemitkin.financie.domain.User::languageCode)
            .filter(SupportedLanguageChecker::isSupported)
            .map(Locale::of)
            .orElse(Locale.getDefault());
    }

    private void processEvent(UpdateReceivedEvent event) {
        Update update = event.getUpdate();
        updateHandlerChain.handleUpdate(update);
    }

    private User updateUser(@NonNull Update update) {
        var from = getFrom(update);
        Long chatId = getChatId(update);
        if (from != null && chatId != null) {
            return userService.saveOrUpdateUser(UserMapper.toSaveOrUpdateUserCommand(from, chatId));
        } else {
            logger.error("Failed to update user info: {}", update);
            throw new RuntimeException();
        }
    }
}
