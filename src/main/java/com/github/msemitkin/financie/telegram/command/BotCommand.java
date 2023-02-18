package com.github.msemitkin.financie.telegram.command;

public enum BotCommand {
    START("/start"),
    HELP("/help"),
    IMPORT("/import"),
    AUTHOR("/author"),
    MONTHLY_STATISTICS("Monthly statistics");

    private final String command;

    BotCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
