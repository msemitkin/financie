package com.github.msemitkin.financie.telegram.callback.command;

public record GetMonthlyCategoryTransactionsCommand(long categoryId, int offset) {
}
