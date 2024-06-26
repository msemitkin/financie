package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.updatehandler.chain.UpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static java.util.Objects.requireNonNull;

@Component
public class DefaultUpdateHandler extends UpdateHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultUpdateHandler.class);
    private final TelegramApi telegramApi;

    public DefaultUpdateHandler(TelegramApi telegramApi) {
        this.telegramApi = telegramApi;
    }

    @Override
    protected boolean canHandle(Update update) {
        return true;
    }

    protected void handleUpdate(Update update) {
        logger.warn("Unrecognized update: {}", update);

        Integer messageId = Optional.ofNullable(update.getMessage())
            .map(Message::getMessageId)
            .orElse(null);

        String sorryMessage = ResourceService.getValue("sorry-message", UserContextHolder.getContext().locale());
        long chatId = requireNonNull(getChatId(update));
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .replyToMessageId(messageId)
            .text(sorryMessage)
            .build();
        telegramApi.execute(sendMessage);
    }
}
