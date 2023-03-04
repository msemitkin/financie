package com.github.msemitkin.financie.telegram.callback.command;

public record GetDailyCategoryTransactionsCommand(long categoryId, int offset) {
}
