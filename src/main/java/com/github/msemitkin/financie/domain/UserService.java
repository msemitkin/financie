package com.github.msemitkin.financie.domain;

import com.github.msemitkin.financie.persistence.entity.UserEntity;
import com.github.msemitkin.financie.persistence.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByTelegramId(long telegramId) {
        UserEntity userEntity = userRepository.getUserEntityByTelegramId(telegramId);
        return mapToUser(userEntity);
    }

    public User saveOrUpdateUser(@NonNull SaveOrUpdateUserCommand command) {
        UserEntity existingUserEntity = userRepository.getUserEntityByTelegramId(command.telegramId());
        Long existingUserId = Optional.ofNullable(existingUserEntity).map(UserEntity::getId).orElse(null);
        UserEntity userEntity = new UserEntity(
            existingUserId,
            command.telegramId(),
            command.chatId(),
            command.telegramUsername(),
            command.firstName(),
            command.lastName()
        );
        UserEntity saved = userRepository.save(userEntity);

        return mapToUser(saved);
    }

    @NonNull
    private User mapToUser(@NonNull UserEntity userEntity) {
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
