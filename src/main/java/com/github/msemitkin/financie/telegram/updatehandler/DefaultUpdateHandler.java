package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.telegram.api.TelegramApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static java.util.Objects.requireNonNull;

@Component
public class DefaultUpdateHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultUpdateHandler.class);
    private final TelegramApi telegramApi;

    public DefaultUpdateHandler(TelegramApi telegramApi) {
        this.telegramApi = telegramApi;
    }

    public void handleUpdate(Update update) {
        logger.warn("Unrecognized update: {}", update);

        Integer messageId = Optional.ofNullable(update.getMessage())
            .map(Message::getMessageId)
            .orElse(null);
        String sorryMessage = """
            Oops! It looks like we're a little confused and couldn't understand your command.
            Don't worry, we'll get our act together soon! In the meantime, \
            try typing /help for a list of commands that we do understand.
                        
            Thanks for being patient with us! 😅
            """;
        long chatId = requireNonNull(getChatId(update));
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .replyToMessageId(messageId)
            .text(sorryMessage)
            .build();
        telegramApi.execute(sendMessage);
    }
}
