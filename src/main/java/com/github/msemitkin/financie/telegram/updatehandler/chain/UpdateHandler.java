package com.github.msemitkin.financie.telegram.updatehandler.chain;

import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class UpdateHandler {
    private UpdateHandler next;

    public void setNext(UpdateHandler next) {
        this.next = next;
    }

    public void processUpdate(Update update) {
        if (canHandle(update)) {
            handleUpdate(update);
        } else if (next != null) {
            next.processUpdate(update);
        } else {
            throw new UnhandledUpdateException();
        }
    }

    protected abstract boolean canHandle(Update update);

    protected abstract void handleUpdate(Update update);
}
