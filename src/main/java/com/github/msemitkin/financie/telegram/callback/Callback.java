package com.github.msemitkin.financie.telegram.callback;

public record Callback<T>(CallbackType callbackType, T payload) {
}
