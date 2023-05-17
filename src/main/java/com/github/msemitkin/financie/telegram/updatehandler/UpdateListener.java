package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.locale.SupportedLanguageChecker;
import com.github.msemitkin.financie.state.SettingsState;
import com.github.msemitkin.financie.state.StateService;
import com.github.msemitkin.financie.state.StateType;
import com.github.msemitkin.financie.telegram.UpdateReceivedEvent;
import com.github.msemitkin.financie.telegram.auth.UserContext;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.transaction.UserMapper;
import com.github.msemitkin.financie.telegram.updatehandler.system.AuthorHandler;
import com.github.msemitkin.financie.telegram.updatehandler.system.HelpHandler;
import com.github.msemitkin.financie.telegram.updatehandler.system.StartMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getFrom;

@Component
public class UpdateListener {
    private static final Logger logger = LoggerFactory.getLogger(UpdateListener.class);

    private final List<UpdateHandler> updateHandlers;
    private final DefaultUpdateHandler defaultUpdateHandler;
    private final UserService userService;
    private final StateService stateService;
    private final SettingsState settingsState;
    private final StartMessageHandler startMessageHandler;
    private final HelpHandler helpHandler;
    private final AuthorHandler authorHandler;

    public UpdateListener(
        List<UpdateHandler> updateHandlers,
        DefaultUpdateHandler defaultUpdateHandler,
        UserService userService,
        StateService stateService,
        SettingsState settingsState,
        StartMessageHandler startMessageHandler,
        HelpHandler helpHandler,
        AuthorHandler authorHandler
    ) {
        this.updateHandlers = updateHandlers;
        this.defaultUpdateHandler = defaultUpdateHandler;
        this.userService = userService;
        this.stateService = stateService;
        this.settingsState = settingsState;
        this.startMessageHandler = startMessageHandler;
        this.helpHandler = helpHandler;
        this.authorHandler = authorHandler;
    }

    @Async
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
            processEvent(event, user);
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
            .map(Locale::new)
            .orElse(Locale.getDefault());
    }

    private void processEvent(UpdateReceivedEvent event, User user) {
        StateType stateType = stateService.getStateType(user.id());
        Update update = event.getUpdate();

        if (startMessageHandler.canHandle(update)) {
            startMessageHandler.handleUpdate(update);
        } else if (helpHandler.canHandle(update)) {
            helpHandler.handleUpdate(update);
        } else if (authorHandler.canHandle(update)) {
            authorHandler.handleUpdate(update);
        } else {
            switch (stateType) {
                case NONE, IDLE -> updateHandlers.stream()
                    .filter(updateHandler -> updateHandler.canHandle(update))
                    .findFirst()
                    .ifPresentOrElse(updateHandler -> updateHandler.handleUpdate(update),
                        () -> defaultUpdateHandler.handleUpdate(update));
                case SETTINGS -> settingsState.handle(update);
            }
        }
    }

    private User updateUser(@NonNull Update update) {
        var from = getFrom(update);
        Long chatId = getChatId(update);
        if (from != null && chatId != null) {
            return userService.saveOrUpdateUser(UserMapper.toSaveOrUpdateUserCommand(from, chatId));
        } else {
            logger.info("Failed to update user info: {}", update);
            throw new RuntimeException();
        }
    }
}
