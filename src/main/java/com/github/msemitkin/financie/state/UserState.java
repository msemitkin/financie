package com.github.msemitkin.financie.state;

public record UserState<T>(
    long userId,
    StateType stateType,
    T context
) {
}
