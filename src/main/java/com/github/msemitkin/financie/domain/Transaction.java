package com.github.msemitkin.financie.domain;

import java.time.LocalDateTime;

public record Transaction(
    long id,
    long userId,
    double amount,
    String category,
    String description,
    LocalDateTime time
) {
}
