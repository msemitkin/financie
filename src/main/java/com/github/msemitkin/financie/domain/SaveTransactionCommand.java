package com.github.msemitkin.financie.domain;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;

public record SaveTransactionCommand(
    long userId,
    double amount,
    @Nonnull String category,
    @Nullable String description,
    @Nullable LocalDateTime utcDateTime
) {
}
