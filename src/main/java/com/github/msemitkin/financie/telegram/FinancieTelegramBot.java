package com.github.msemitkin.financie.telegram;

import com.github.msemitkin.financie.domain.SaveTransactionCommand;
import com.github.msemitkin.financie.domain.TransactionService;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class FinancieTelegramBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(FinancieTelegramBot.class);
    private static final int CATEGORY_NAME_MAX_LENGTH = 64;

    private final String username;
    private final TransactionService transactionService;

    public FinancieTelegramBot(
        @Value("${bot.telegram.username}") String username,
        @Value("${bot.telegram.token}") String botToken,
        TransactionService transactionService
    ) {
        super(botToken);
        this.username = username;
        this.transactionService = transactionService;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onRegister() {
        logger.info("Bot successfully registered");
        super.onRegister();
    }

    @Override
    public void onUpdateReceived(Update update) {
        logger.info("Received update");
        Message message = update.getMessage();
        if (message.hasText()) {
            try {
                User sender = message.getFrom();
                String messageText = message.getText();

                validateTransaction(messageText);

                long userId = transactionService.getOrCreateUserByTelegramId(sender.getId());
                double amount = parseAmount(messageText);
                String category = parseCategory(messageText);
                transactionService.saveTransaction(new SaveTransactionCommand(userId, amount, category, null));
                sendMessage(update.getMessage().getChatId().toString(), "Successfully saved", message.getMessageId());
                //TODO reply with statistics
            } catch (IllegalArgumentException e) {
                sendMessage(update.getMessage().getChatId().toString(), "Failed to process", message.getMessageId());
            }
        }
    }

    private double parseAmount(String messageText) {
        return Double.parseDouble(messageText.substring(0, messageText.indexOf(" ")));
    }

    private String parseCategory(String messageText) {
        return messageText.substring(messageText.indexOf(" ") + 1);
    }

    private void validateTransaction(String messageText) {
        String[] split = messageText.split(" ", 2);
        if (
            !(split.length == 2
              && NumberUtils.isParsable(split[0])
              && split[1].length() <= CATEGORY_NAME_MAX_LENGTH)
        ) {
            throw new IllegalArgumentException("Cannot parse message");
        }
    }

    private void sendMessage(String chatId, String text, @Nullable Integer replyToMessageId) {
        try {
            SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyToMessageId(replyToMessageId)
                .build();
            this.execute(sendMessage);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }
}
