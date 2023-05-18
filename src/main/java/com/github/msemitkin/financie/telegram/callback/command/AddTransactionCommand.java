package com.github.msemitkin.financie.telegram.callback.command;

import java.time.LocalDate;

public record AddTransactionCommand(LocalDate date) {
}
