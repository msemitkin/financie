package com.github.msemitkin.financie.state;

import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContext;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.keyboard.KeyboardService;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import com.github.msemitkin.financie.telegram.util.UpdateUtil;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import static com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher.textCommandMatcher;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getMessage;
import static java.util.Objects.requireNonNull;

@Component
public class MenuState implements State {
    private static final Logger logger = LoggerFactory.getLogger(MenuState.class);

    private final UpdateMatcher importMatcher =
        textCommandMatcher(ResourceService.getValues("button.import"));
    private final UpdateMatcher settingsMatcher =
        textCommandMatcher(ResourceService.getValues("button.settings"));
    private final UpdateMatcher backMatcher =
        textCommandMatcher(ResourceService.getValues("button.back"));

    private final TelegramApi telegramApi;
    private final StateService stateService;
    private final KeyboardService keyboardService;
    private final UserService userService;
    private final ResourceLoader resourceLoader;
    private final String templatePath;
    private final String outputFileName;

    public MenuState(
        TelegramApi telegramApi,
        StateService stateService,
        KeyboardService keyboardService,
        UserService userService,
        ResourceLoader resourceLoader,
        @Value("${com.github.msemitkin.financie.import.template-path}") String templatePath,
        @Value("${com.github.msemitkin.financie.import.output-file-name}") String outputFileName
    ) {
        this.telegramApi = telegramApi;
        this.stateService = stateService;
        this.keyboardService = keyboardService;
        this.userService = userService;
        this.resourceLoader = resourceLoader;
        this.templatePath = templatePath;
        this.outputFileName = outputFileName;
    }


    @Override
    public void handle(Update update) {
        UserContext userContext = UserContextHolder.getContext();
        Locale locale = userContext.locale();
        Long chatId = UpdateUtil.getChatId(update);
        long senderTelegramId = UpdateUtil.getSenderTelegramId(update);
        User user = userService.getUserByTelegramId(senderTelegramId);
        if (settingsMatcher.match(update)) {
            String messageTemplate = ResourceService.getValue("message.settings", locale);
            String message = StringSubstitutor.replace(messageTemplate,
                Map.of("language", locale.getDisplayLanguage(locale),
                    "timezone", userContext.timeZone().getID()));
            StateType nextState = StateType.SETTINGS;
            telegramApi.execute(SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .replyMarkup(keyboardService.getKeyboardForState(nextState, locale))
                .build());
            stateService.setStateType(user.id(), nextState);
        } else if (importMatcher.match(update)) {
            StateType nextState = StateType.IMPORT;

            telegramApi.execute(SendMessage.builder()
                .chatId(chatId)
                .text(ResourceService.getValue("csv-offer-template-message", locale))
                .replyMarkup(keyboardService.getKeyboardForState(StateType.IMPORT, locale))
                .build());
            try (InputStream is = resourceLoader.getResource(templatePath).getInputStream()) {
                InputFile template = new InputFile(is, outputFileName);
                telegramApi.execute(SendDocument.builder()
                    .chatId(chatId)
                    .document(template)
                    .build());
            } catch (IOException e) {
                logger.error("Failed to load template");
            }

            stateService.setStateType(user.id(), nextState);
        } else if (backMatcher.match(update)) {
            StateType nextState = StateType.IDLE;
            telegramApi.execute(SendMessage.builder()
                .chatId(chatId)
                .text(requireNonNull(getMessage(update)))
                .replyMarkup(keyboardService.getKeyboardForState(nextState, locale))
                .build());
            stateService.setStateType(user.id(), nextState);
        } else {
            telegramApi.execute(SendMessage.builder()
                .chatId(chatId)
                .text(ResourceService.getValue("sorry-message", locale))
                .build());
        }
    }
}
