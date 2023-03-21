package com.github.msemitkin.financie.telegram.updatehandler.import_;

import com.github.msemitkin.financie.csvimport.CsvFileHistoryImportService;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.updatehandler.UpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.nio.file.Files;
import java.util.Locale;
import java.util.Optional;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getFrom;

@Component
public class CsvFileHandler implements UpdateHandler {
    private static final Logger logger = LoggerFactory.getLogger(CsvFileHandler.class);

    private final TelegramApi telegramApi;
    private final CsvFileHistoryImportService csvFileHistoryImportService;
    private final UserService userService;

    public CsvFileHandler(
        TelegramApi telegramApi,
        CsvFileHistoryImportService csvFileHistoryImportService,
        UserService userService
    ) {
        this.telegramApi = telegramApi;
        this.csvFileHistoryImportService = csvFileHistoryImportService;
        this.userService = userService;
    }

    @Override
    public boolean canHandle(Update update) {
        return Optional.ofNullable(update.getMessage())
            .map(Message::getDocument)
            .map(Document::getMimeType)
            .map("text/csv"::equals)
            .orElse(false);
    }

    @Override
    public void handleUpdate(Update update) {
        Long chatId = update.getMessage().getChatId();
        Integer messageId = update.getMessage().getMessageId();

        Locale userLocale = UserContextHolder.getContext().locale();

        sendMessage(chatId, messageId, ResourceService.getValue("csv-on-upload-reply-message", userLocale));
        try {
            long senderTelegramId = getFrom(update).getId();
            long userId = userService.getUserByTelegramId(senderTelegramId).id();
            File fileReference = getFile(update);
            java.io.File file = telegramApi.downloadFile(fileReference);
            byte[] bytes = Files.readAllBytes(file.toPath());

            csvFileHistoryImportService.importTransactions(userId, bytes);

            sendMessage(chatId, messageId, ResourceService.getValue("csv-file-processed-message", userLocale));
        } catch (Exception e) {
            logger.info("Failed to import transactions", e);

            sendMessage(chatId, messageId, ResourceService.getValue("csv-failed-to-process-file-message", userLocale));
        }
    }

    private void sendMessage(Long chatId, Integer messageId, String text) {
        telegramApi.execute(SendMessage.builder()
            .chatId(chatId)
            .replyToMessageId(messageId)
            .text(text)
            .build());
    }

    private File getFile(Update update) {
        Document document = update.getMessage().getDocument();
        GetFile getFile = GetFile.builder()
            .fileId(document.getFileId())
            .build();
        return telegramApi.execute(getFile);
    }

}
