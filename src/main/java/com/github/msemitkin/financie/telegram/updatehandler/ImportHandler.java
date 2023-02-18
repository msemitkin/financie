package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.telegram.api.TelegramApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static java.util.Objects.requireNonNull;

@Component
public class ImportHandler extends AbstractTextCommandHandler {
    private final TelegramApi telegramApi;
    private final InputFile template;

    public ImportHandler(
        TelegramApi telegramApi,
        @Value("${com.github.msemitkin.financie.import-file-template-filename}") String fileName
    ) {
        super("/import");
        this.telegramApi = telegramApi;
        this.template = new InputFile(new File(getClass().getClassLoader().getResource(fileName).getFile()), "template.csv");
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
                                
                Below is a template you can use
                """)
            .build());

        SendDocument build = SendDocument.builder()
            .chatId(chatId)
            .document(template)
            .build();
        telegramApi.execute(build);
    }
}
