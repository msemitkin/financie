package com.github.msemitkin.financie;

import com.github.msemitkin.financie.telegram.UpdateReceivedEvent;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;

@Configuration
public class BotConfig {
    private static final Logger logger = LoggerFactory.getLogger(BotConfig.class);

    @Bean
    public SpringTelegramWebhookBot bot(
        @Value("financie") String botPath,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        return new SpringTelegramWebhookBot(
            botPath,
            update -> {
                applicationEventPublisher.publishEvent(new UpdateReceivedEvent(update));
                return null;
            },
            () -> logger.info("No webhook to set"),
            () -> logger.info("Webhook not deleted")
        );
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    @Bean
    public OkHttpTelegramClient telegramClient(
        OkHttpClient okHttpClient,
        @Value("${bot.telegram.token}") String token

    ) {
        return new OkHttpTelegramClient(okHttpClient, token);
    }

}
