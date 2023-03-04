package com.github.msemitkin.financie.domain;

public record SaveOrUpdateUserCommand(
    long telegramId,
    long chatId,
    String firstName,
    String lastName,
    String telegramUsername
) {
}
