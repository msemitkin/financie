package com.github.msemitkin.financie.domain;

import com.github.msemitkin.financie.persistence.entity.UserEntity;
import com.github.msemitkin.financie.persistence.mapper.UserMapper;
import com.github.msemitkin.financie.persistence.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static com.github.msemitkin.financie.persistence.mapper.UserMapper.mapToUser;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public UserService(
        UserRepository userRepository,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.userRepository = userRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public User getUserById(long userId) {
        return userRepository.findById(userId).map(UserMapper::mapToUser).orElse(null);
    }

    public User getUserByTelegramId(long telegramId) {
        UserEntity userEntity = userRepository.getUserEntityByTelegramId(telegramId);
        return mapToUser(userEntity);
    }

    public User saveOrUpdateUser(@NonNull SaveOrUpdateUserCommand command) {
        UserEntity userById = userRepository.getUserEntityByTelegramId(command.telegramId());
        User existingUser = Optional.ofNullable(userById)
            .map(UserMapper::mapToUser)
            .orElse(null);

        if (existingUser == null) {
            return saveUser(command);
        } else {
            return updateUser(existingUser, command);
        }
    }

    private User saveUser(SaveOrUpdateUserCommand command) {
        UserEntity userEntity = new UserEntity(
            null,
            command.telegramId(),
            command.chatId(),
            command.telegramUsername(),
            command.firstName(),
            command.lastName(),
            command.languageCode()
        );
        return mapToUser(userRepository.save(userEntity));
    }

    private User updateUser(User existing, SaveOrUpdateUserCommand command) {
        UserEntity userEntity = new UserEntity(
            existing.id(),
            command.telegramId(),
            command.chatId(),
            command.telegramUsername(),
            command.firstName(),
            command.lastName(),
            command.languageCode()
        );
        User updated = mapToUser(userRepository.save(userEntity));

        if (!Objects.equals(existing.languageCode(), command.languageCode())) {
            applicationEventPublisher.publishEvent(new UserLanguageUpdatedEvent(existing.id()));
        }

        return updated;
    }
}
