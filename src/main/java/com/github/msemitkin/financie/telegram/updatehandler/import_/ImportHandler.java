package com.github.msemitkin.financie.telegram.updatehandler.import_;

import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractTextCommandHandler;
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

import static com.github.msemitkin.financie.telegram.command.BotCommand.IMPORT;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static java.util.Objects.requireNonNull;

@Component
public class ImportHandler extends AbstractTextCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ImportHandler.class);

    private final TelegramApi telegramApi;
    private final ResourceLoader resourceLoader;
    private final String templatePath;
    private final String outputFileName;

    public ImportHandler(
        TelegramApi telegramApi,
        @Value("${com.github.msemitkin.financie.import.template-path}") String templatePath,
        @Value("${com.github.msemitkin.financie.import.output-file-name}") String outputFileName,
        ResourceLoader resourceLoader
    ) {
        super(IMPORT.getCommand());
        this.telegramApi = telegramApi;
        this.resourceLoader = resourceLoader;
        this.templatePath = templatePath;
        this.outputFileName = outputFileName;
    }

    @Override
    public void handleUpdate(Update update) {
        Long chatId = requireNonNull(getChatId(update));
        telegramApi.execute(SendMessage.builder()
            .chatId(chatId)
            .text("""
                Hey there!
                To import your transactions, simply upload a CSV file with the \
                'amount', 'category', 'datetime' (in the 'yyyy-MM-dd HH:mm' format) columns  using the \
                paperclip icon, and the bot will handle the rest!
                                
                Please, note that recording of savings is not supported yet.
                                
                Below is a template you can use
                """)
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
    }
}