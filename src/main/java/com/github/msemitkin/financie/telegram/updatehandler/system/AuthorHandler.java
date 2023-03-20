package com.github.msemitkin.financie.telegram.updatehandler.system;

import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractTextCommandHandler;
import com.github.msemitkin.financie.telegram.util.MarkdownUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static java.util.Objects.requireNonNull;

@Component
public class AuthorHandler extends AbstractTextCommandHandler {
    private final TelegramApi telegramApi;

    public AuthorHandler(TelegramApi telegramApi) {
        super(BotCommand.AUTHOR.getCommand());
        this.telegramApi = telegramApi;
    }

    @Override
    public void handleUpdate(Update update) {
        Long chatId = requireNonNull(getChatId(update));
        String text = MarkdownUtil.escapeMarkdownV2(ResourceService.getValue("author-message"));
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .parseMode(ParseMode.MARKDOWNV2)
            .build();
        telegramApi.execute(sendMessage);
    }
}
