package com.github.msemitkin.financie.telegram.updatehandler.chain;

import org.telegram.telegrambots.meta.api.objects.Update;

public class UpdateHandlerChain {
    private final UpdateHandler rootHandler;

    public UpdateHandlerChain(UpdateHandler rootHandler) {
        this.rootHandler = rootHandler;
    }

    public void handleUpdate(Update update) {
        rootHandler.processUpdate(update);
    }

    public static UpdateHandlerChain.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UpdateHandler root;
        private UpdateHandler tail;

        public Builder addHandler(UpdateHandler updateHandler) {
            if (root == null) {
                root = updateHandler;
            }
            if (tail != null) {
                tail.setNext(updateHandler);
            }
            tail = updateHandler;
            return this;
        }

        public UpdateHandlerChain build() {
            return new UpdateHandlerChain(root);
        }
    }
}
