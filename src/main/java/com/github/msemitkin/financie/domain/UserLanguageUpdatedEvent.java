package com.github.msemitkin.financie.domain;

import org.springframework.context.ApplicationEvent;

public class UserLanguageUpdatedEvent extends ApplicationEvent {
    public UserLanguageUpdatedEvent(long userId) {
        super(userId);
    }

    public long getUserId() {
        return (long) getSource();
    }
}
