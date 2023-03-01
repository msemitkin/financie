package com.github.msemitkin.financie.telegram.updatehandler.import_;

import com.github.msemitkin.financie.csvimport.CsvFileHistoryImportService;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
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
import java.util.Optional;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;
import static java.util.Objects.requireNonNull;

@Component
public class CsvFileHandler implements UpdateHandler {
    private static final Logger logger = LoggerFactory.getLogger(CsvFileHandler.class);

    private final TelegramApi telegramApi;
    private final CsvFileHistoryImportService csvFileHistoryImportService;
    private final TransactionService transactionService;

    public CsvFileHandler(
        TelegramApi telegramApi,
        CsvFileHistoryImportService csvFileHistoryImportService,
        TransactionService transactionService
    ) {
        this.telegramApi = telegramApi;
        this.csvFileHistoryImportService = csvFileHistoryImportService;
        this.transactionService = transactionService;
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
        sendMessage(chatId, messageId, """
            Got it! We're processing your file now. This might take a few moments, \
            so sit back and relax while we work our magic. We'll let you know once finished
            """);
        try {
            long userTelegramId = requireNonNull(getSenderTelegramId(update));
            long userId = transactionService.getOrCreateUserByTelegramId(userTelegramId);
            File fileReference = getFile(update);
            java.io.File file = telegramApi.downloadFile(fileReference);
            byte[] bytes = Files.readAllBytes(file.toPath());

            csvFileHistoryImportService.importTransactions(userId, bytes);

            sendMessage(chatId, messageId, "Your file has been processed!");
        } catch (Exception e) {
            logger.info("Failed to import transactions", e);

            sendMessage(chatId, messageId,
                "Failed to import transactions, please check file format and try again");
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
