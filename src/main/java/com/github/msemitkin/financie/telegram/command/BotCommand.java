package com.github.msemitkin.financie.telegram.command;

public enum BotCommand {
    START("/start"),
    AUTHOR("/author"),
    HELP("/help"),
    MONTHLY_STATISTICS("Monthly statistics");

    private final String command;

    BotCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
