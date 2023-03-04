package com.github.msemitkin.financie.persistence.mapper;

import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.persistence.entity.UserEntity;
import org.springframework.lang.NonNull;

public class UserMapper {
    private UserMapper() {
    }

    @NonNull
    public static User mapToUser(@NonNull UserEntity userEntity) {
        return new User(
            userEntity.getId(),
            userEntity.getTelegramId(),
            userEntity.getTelegramChatId(),
            userEntity.getTelegramUsername(),
            userEntity.getFirstName(),
            userEntity.getLastName()
        );
    }
}
