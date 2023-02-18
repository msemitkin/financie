package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.github.msemitkin.financie.telegram.util.MarkdownUtil.escapeMarkdownV2;
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
        String text = escapeMarkdownV2("""
            Oopsie daisy! It looks like our team of highly trained monkeys forgot to add the information \
            about the author.
            Don't worry though, they're working on it right now and it'll be added soon.
            Thanks for your patience and please don't feed the monkeys!
            """);
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .parseMode(ParseMode.MARKDOWNV2)
            .build();
        telegramApi.execute(sendMessage);
    }
}
