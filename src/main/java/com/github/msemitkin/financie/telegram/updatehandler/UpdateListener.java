package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.telegram.UpdateReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class UpdateListener {
    private static final Logger logger = LoggerFactory.getLogger(UpdateListener.class);

    private final List<UpdateHandler> updateHandlers;

    public UpdateListener(List<UpdateHandler> updateHandlers) {
        this.updateHandlers = updateHandlers;
    }

    @EventListener(UpdateReceivedEvent.class)
    public void onUpdateReceived(UpdateReceivedEvent event) {
        Update update = event.getUpdate();
        updateHandlers.stream()
            .filter(updateHandler -> updateHandler.canHandle(update))
            .findFirst()
            .ifPresentOrElse(updateHandler -> updateHandler.handleUpdate(update),
                () -> logger.warn("Unrecognized update: {}", update));
    }
}
