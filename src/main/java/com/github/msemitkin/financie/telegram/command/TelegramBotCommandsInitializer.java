package com.github.msemitkin.financie.telegram.command;

import com.github.msemitkin.financie.locale.LanguageCode;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class TelegramBotCommandsInitializer implements InitializingBean {
    private final TelegramApi telegramApi;

    public TelegramBotCommandsInitializer(TelegramApi telegramApi) {
        this.telegramApi = telegramApi;
    }

    @Override
    public void afterPropertiesSet() {
        initCommands();
    }

    private void initCommands() {
        List<Locale> locales = Arrays
            .stream(LanguageCode.values())
            .map(LanguageCode::getCode)
            .map(Locale::of)
            .toList();
        var botCommands = Arrays
            .stream(BotCommand.values())
            .toList();
        for (Locale locale : locales) {
            var commandsForLocale = getCommandsForLocale(locale, botCommands);
            telegramApi.execute(SetMyCommands.builder()
                .languageCode(locale.getLanguage())
                .commands(commandsForLocale)
                .scope(BotCommandScopeDefault.builder().build())
                .build());
        }
    }

    private List<org.telegram.telegrambots.meta.api.objects.commands.BotCommand> getCommandsForLocale(
        Locale locale,
        List<BotCommand> botCommands
    ) {
        return botCommands.stream()
            .map(command -> org.telegram.telegrambots.meta.api.objects.commands.BotCommand.builder()
                .command(command.getCommand())
                .description(ResourceService.getValue(command.getDescriptionCode(), locale))
                .build())
            .toList();
    }
}
