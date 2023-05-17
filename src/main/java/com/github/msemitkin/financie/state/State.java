package com.github.msemitkin.financie.state;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface State {
    void handle(Update update);
}
