package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.csv.CsvFileHistoryImportService;
import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.state.StateService;
import com.github.msemitkin.financie.state.StateType;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContext;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.keyboard.KeyboardService;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.nio.file.Files;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher.textCommandMatcher;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getMessage;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;

@Component
public class ImportStateHandler extends BaseUpdateHandler {
    private static final Logger logger = LoggerFactory.getLogger(ImportStateHandler.class);

    private final UpdateMatcher importMatcher = update -> Optional
        .ofNullable(update.getMessage())
        .map(Message::getDocument)
        .map(Document::getMimeType)
        .map(Set.of("text/comma-separated-values", "text/csv")::contains)
        .orElse(false);
    private final UpdateMatcher cancelMatcher = textCommandMatcher(ResourceService.getValues("button.cancel"));

    private final UserService userService;
    private final TelegramApi telegramApi;
    private final KeyboardService keyboardService;
    private final StateService stateService;
    private final CsvFileHistoryImportService csvFileHistoryImportService;

    public ImportStateHandler(
        UserService userService,
        TelegramApi telegramApi,
        KeyboardService keyboardService,
        StateService stateService,
        CsvFileHistoryImportService csvFileHistoryImportService
    ) {
        super(UpdateMatcher.userStateTypeUpdateMatcher(userService, stateService, StateType.IMPORT));
        this.userService = userService;
        this.telegramApi = telegramApi;
        this.keyboardService = keyboardService;
        this.stateService = stateService;
        this.csvFileHistoryImportService = csvFileHistoryImportService;
    }

    @Override
    protected void handleUpdate(Update update) {
        UserContext userContext = UserContextHolder.getContext();
        Locale userLocale = userContext.locale();
        ZoneId timeZoneId = userContext.timeZone().toZoneId();

        Long chatId = getChatId(update);
        long senderTelegramId = getSenderTelegramId(update);
        User user = userService.getUserByTelegramId(senderTelegramId);
        if (importMatcher.match(update)) {
            StateType nextState = StateType.IDLE;
            Integer messageId = update.getMessage().getMessageId();
            telegramApi.execute(SendMessage.builder()
                .chatId(chatId)
                .replyToMessageId(messageId)
                .text(ResourceService.getValue("csv-on-upload-reply-message", userLocale))
                .replyMarkup(keyboardService.getKeyboardForState(nextState, userLocale))
                .build());
            stateService.setStateType(user.id(), nextState);
            try {
                File fileReference = getFile(update);
                java.io.File file = telegramApi.downloadFile(fileReference);
                byte[] bytes = Files.readAllBytes(file.toPath());

                csvFileHistoryImportService.importTransactions(user.id(), bytes, timeZoneId);

                telegramApi.execute(SendMessage.builder()
                    .chatId(chatId)
                    .replyToMessageId(messageId)
                    .text(ResourceService.getValue("csv-file-processed-message", userLocale))
                    .build());
            } catch (Exception e) {
                logger.info("Failed to import transactions", e);

                telegramApi.execute(SendMessage.builder()
                    .chatId(chatId)
                    .replyToMessageId(messageId)
                    .text(ResourceService.getValue("csv-failed-to-process-file-message", userLocale))
                    .build());
            }
        } else if (cancelMatcher.match(update)) {
            StateType nextState = StateType.IDLE;
            telegramApi.execute(SendMessage.builder()
                .chatId(chatId)
                .text(getMessage(update))
                .replyMarkup(keyboardService.getKeyboardForState(nextState, userLocale))
                .build());
            stateService.setStateType(user.id(), nextState);
        } else {
            telegramApi.execute(SendMessage.builder()
                .chatId(chatId)
                .text(ResourceService.getValue("import.sorry-message", userLocale))
                .build());
        }
    }

    private File getFile(Update update) {
        Document document = update.getMessage().getDocument();
        GetFile getFile = GetFile.builder()
            .fileId(document.getFileId())
            .build();
        return telegramApi.execute(getFile);
    }
}
