package com.github.msemitkin.financie.domain;

public record User(
    long id,
    Long telegramId,
    Long telegramChatId,
    String telegramUsername,
    String firstName,
    String lastName
) {
}
