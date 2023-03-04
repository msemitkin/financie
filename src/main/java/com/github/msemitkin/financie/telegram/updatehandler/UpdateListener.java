package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.telegram.UpdateReceivedEvent;
import com.github.msemitkin.financie.telegram.transaction.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getFrom;

@Component
public class UpdateListener {
    private static final Logger logger = LoggerFactory.getLogger(UpdateListener.class);

    private final List<UpdateHandler> updateHandlers;
    private final DefaultUpdateHandler defaultUpdateHandler;
    private final UserService userService;

    public UpdateListener(
        List<UpdateHandler> updateHandlers,
        DefaultUpdateHandler defaultUpdateHandler,
        UserService userService
    ) {
        this.updateHandlers = updateHandlers;
        this.defaultUpdateHandler = defaultUpdateHandler;
        this.userService = userService;
    }

    @EventListener(UpdateReceivedEvent.class)
    public void onUpdateReceived(UpdateReceivedEvent event) {
        Update update = event.getUpdate();
        updateUser(update);

        updateHandlers.stream()
            .filter(updateHandler -> updateHandler.canHandle(update))
            .findFirst()
            .ifPresentOrElse(updateHandler -> updateHandler.handleUpdate(update),
                () -> defaultUpdateHandler.handleUpdate(update));
    }

    private void updateUser(@NonNull Update update) {
        User from = getFrom(update);
        Long chatId = getChatId(update);
        if (from != null && chatId != null) {
            userService.saveOrUpdateUser(UserMapper.toSaveOrUpdateUserCommand(from, chatId));
        } else {
            logger.info("Failed to update user info: {}", update);
        }
    }
}
