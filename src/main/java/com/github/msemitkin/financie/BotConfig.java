package com.github.msemitkin.financie;

import com.github.msemitkin.financie.telegram.UpdateReceivedEvent;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;

import java.util.Optional;

@Configuration
public class BotConfig {
    private static final Logger logger = LoggerFactory.getLogger(BotConfig.class);

    @Bean
    public SpringTelegramWebhookBot bot(
        @Value("financie") String botPath,
        ApplicationEventPublisher applicationEventPublisher,
        TelegramApi telegramApi,
        Optional<SetWebhook> setWebhook,
        Optional<DeleteWebhook> deleteWebhook
    ) {
        return new SpringTelegramWebhookBot(
            botPath,
            update -> {
                applicationEventPublisher.publishEvent(new UpdateReceivedEvent(update));
                return null;
            },
            setWebhook
                .<Runnable>map(value -> () -> {
                    boolean result = telegramApi.execute(value);
                    logger.info("Set webhook result: {}", result ? "SUCCESS" : "FAIL");
                })
                .orElse(() -> logger.info("No webhook to set")),
            deleteWebhook
                .<Runnable>map(webhook -> () -> {
                    boolean result = telegramApi.execute(webhook);
                    logger.info("Delete webhook result: {}", result ? "SUCCESS" : "FAIL");
                })
                .orElse(() -> logger.info("Webhook not deleted"))
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

//            @Bean
    public SetWebhook setWebhook(
        @Value("${bot.telegram.url}") String url,
        @Value("${bot.telegram.webhook-secret-token}") String webhookSecretToken
    ) {
        return SetWebhook.builder()
            .url(url)
            .secretToken(webhookSecretToken)
            .build();
    }

//        @Bean
    public DeleteWebhook deleteWebhook() {
        return DeleteWebhook.builder()
            .build();
    }

}
