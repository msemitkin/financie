package com.github.msemitkin.financie.domain;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public record SaveTransactionCommand(
    long userId,
    double amount,
    @Nonnull String category,
    @Nullable String description
) {
}
