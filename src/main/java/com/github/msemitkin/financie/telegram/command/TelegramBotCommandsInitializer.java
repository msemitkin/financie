package com.github.msemitkin.financie.telegram.command;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class TelegramBotCommandsInitializer implements CommandLineRunner {
    private final AbsSender absSender;
    private final List<BotCommand> commands;

    public TelegramBotCommandsInitializer(
        AbsSender absSender,
        List<BotCommand> commands
    ) {
        this.absSender = absSender;
        this.commands = commands;
    }

    @Override
    public void run(String... args) throws Exception {
        initCommands();
    }

    private void initCommands() throws TelegramApiException {
        absSender.execute(SetMyCommands.builder()
            .commands(commands).scope(BotCommandScopeDefault.builder().build())
            .build());
    }
}
