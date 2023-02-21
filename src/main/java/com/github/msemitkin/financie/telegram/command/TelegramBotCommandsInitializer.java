package com.github.msemitkin.financie.telegram.command;

import com.github.msemitkin.financie.telegram.api.TelegramApi;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;

import java.util.List;

@Component
public class TelegramBotCommandsInitializer implements CommandLineRunner {
    private final TelegramApi telegramApi;
    private final List<BotCommand> commands;

    public TelegramBotCommandsInitializer(
        TelegramApi telegramApi,
        List<BotCommand> commands
    ) {
        this.telegramApi = telegramApi;
        this.commands = commands;
    }

    @Override
    public void run(String... args) {
        initCommands();
    }

    private void initCommands() {
        telegramApi.execute(SetMyCommands.builder()
            .commands(commands).scope(BotCommandScopeDefault.builder().build())
            .build());
    }
}
