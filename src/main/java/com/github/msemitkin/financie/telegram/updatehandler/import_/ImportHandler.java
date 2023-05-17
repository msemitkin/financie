package com.github.msemitkin.financie.telegram.updatehandler.import_;

import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.updatehandler.BaseUpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
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
//TODO move import to menu
public class ImportHandler extends BaseUpdateHandler {
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
        super(UpdateMatcher.textCommandMatcher(IMPORT.getCommand()));
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
            .text(ResourceService.getValue("csv-offer-template-message", UserContextHolder.getContext().locale()))
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
