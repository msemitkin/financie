package com.github.msemitkin.financie.domain;

public record User(
    Long id,
    Long telegramId,
    Long telegramChatId,
    String telegramUsername,
    String firstName,
    String lastName
) {
}
