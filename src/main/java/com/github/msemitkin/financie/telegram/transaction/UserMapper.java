package com.github.msemitkin.financie.telegram.transaction;

import com.github.msemitkin.financie.domain.SaveOrUpdateUserCommand;
import org.springframework.lang.NonNull;
import org.telegram.telegrambots.meta.api.objects.User;

public class UserMapper {
    private UserMapper() {
    }

    public static SaveOrUpdateUserCommand toSaveOrUpdateUserCommand(@NonNull User user, long chatId) {
        return new SaveOrUpdateUserCommand(
            user.getId(),
            chatId,
            user.getFirstName(),
            user.getLastName(),
            user.getUserName(),
            user.getLanguageCode()
        );
    }
}
