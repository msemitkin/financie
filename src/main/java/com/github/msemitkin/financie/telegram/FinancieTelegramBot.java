package com.github.msemitkin.financie.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class FinancieTelegramBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(FinancieTelegramBot.class);

    private final String username;
    private final ApplicationEventPublisher applicationEventPublisher;

    public FinancieTelegramBot(
        @Value("${bot.telegram.username}") String username,
        @Value("${bot.telegram.token}") String botToken,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        super(botToken);
        this.username = username;
        this.applicationEventPublisher = applicationEventPublisher;
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
        applicationEventPublisher.publishEvent(new UpdateReceivedEvent(update));
    }
}
