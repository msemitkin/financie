package com.github.msemitkin.financie.telegram;

import org.springframework.context.ApplicationEvent;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UpdateReceivedEvent extends ApplicationEvent {

    public UpdateReceivedEvent(Update update) {
        super(update);
    }

    public Update getUpdate() {
        return (Update) super.source;
    }
}
