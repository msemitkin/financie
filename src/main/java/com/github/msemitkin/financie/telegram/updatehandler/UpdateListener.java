package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.telegram.UpdateReceivedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class UpdateListener {
    private final List<UpdateHandler> updateHandlers;
    private final DefaultUpdateHandler defaultUpdateHandler;

    public UpdateListener(List<UpdateHandler> updateHandlers, DefaultUpdateHandler defaultUpdateHandler) {
        this.updateHandlers = updateHandlers;
        this.defaultUpdateHandler = defaultUpdateHandler;
    }

    @EventListener(UpdateReceivedEvent.class)
    public void onUpdateReceived(UpdateReceivedEvent event) {
        Update update = event.getUpdate();
        updateHandlers.stream()
            .filter(updateHandler -> updateHandler.canHandle(update))
            .findFirst()
            .ifPresentOrElse(updateHandler -> updateHandler.handleUpdate(update),
                () -> defaultUpdateHandler.handleUpdate(update));
    }
}
