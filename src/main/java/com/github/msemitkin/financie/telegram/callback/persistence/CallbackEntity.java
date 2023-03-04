package com.github.msemitkin.financie.telegram.callback.persistence;

import com.github.msemitkin.financie.telegram.callback.CallbackType;

import java.util.UUID;

public record CallbackEntity(UUID id, CallbackType type, String payload) {

}
