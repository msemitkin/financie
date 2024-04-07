package com.github.msemitkin.financie.telegram;

import com.github.msemitkin.financie.locale.LanguageCode;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import org.springframework.beans.factory.InitializingBean;
import org.telegram.telegrambots.meta.api.methods.description.SetMyDescription;
import org.telegram.telegrambots.meta.api.methods.description.SetMyShortDescription;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

//@Component
public class BotDescriptionSetter implements InitializingBean {
    private final TelegramApi telegramApi;

    public BotDescriptionSetter(TelegramApi telegramApi) {
        this.telegramApi = telegramApi;
    }

    @Override
    public void afterPropertiesSet() {
        updateDescription();
        updateShortDescription();
    }

    private void updateDescription() {
        Map<String, String> descriptions = Arrays
            .stream(LanguageCode.values())
            .map(LanguageCode::getCode)
            .map(Locale::of)
            .collect(Collectors.toMap(
                Locale::getLanguage,
                locale -> ResourceService.getValue("bot.description", locale)));
        descriptions.forEach((languageCode, description) ->
            telegramApi.execute(SetMyDescription.builder()
                .languageCode(languageCode)
                .description(description)
                .build()));
    }

    private void updateShortDescription() {
        Map<String, String> shortDescriptions = Arrays
            .stream(LanguageCode.values())
            .map(LanguageCode::getCode)
            .map(Locale::of)
            .collect(Collectors.toMap(
                Locale::getLanguage,
                locale -> ResourceService.getValue("bot.short-description", locale)));
        shortDescriptions.forEach((languageCode, shortDescription) ->
            telegramApi.execute(SetMyShortDescription.builder()
                .languageCode(languageCode)
                .shortDescription(shortDescription)
                .build()));
    }
}
