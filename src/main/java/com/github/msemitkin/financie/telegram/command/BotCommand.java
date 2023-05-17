package com.github.msemitkin.financie.telegram.command;

public enum BotCommand {
    START("/start", "command.start.description"),
    HELP("/help", "command.help.description"),
    AUTHOR("/author", "command.author.description");

    private final String command;
    private final String descriptionCode;

    BotCommand(String command, String descriptionCode) {
        this.command = command;
        this.descriptionCode = descriptionCode;
    }

    public String getCommand() {
        return command;
    }

    public String getDescriptionCode() {
        return descriptionCode;
    }
}
