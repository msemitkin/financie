package com.github.msemitkin.financie.telegram.command;

import com.github.msemitkin.financie.resources.ResourceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import static com.github.msemitkin.financie.telegram.command.BotCommand.AUTHOR;
import static com.github.msemitkin.financie.telegram.command.BotCommand.HELP;
import static com.github.msemitkin.financie.telegram.command.BotCommand.START;

@Configuration
public class CommandsConfig {

    @Order(1)
    @Bean
    public BotCommand startCommand() {
        return BotCommand.builder()
            .command(START.getCommand())
            .description(ResourceService.getValue(START.getDescriptionCode()))
            .build();
    }

    @Order(2)
    @Bean
    public BotCommand helpCommand() {
        return BotCommand.builder()
            .command(HELP.getCommand())
            .description(ResourceService.getValue(HELP.getDescriptionCode()))
            .build();
    }

    @Order(3)
    @Bean
    public BotCommand authorCommand() {
        return BotCommand.builder()
            .command(AUTHOR.getCommand())
            .description(ResourceService.getValue(AUTHOR.getDescriptionCode()))
            .build();
    }

}
