package com.github.msemitkin.financie.domain;

import com.github.msemitkin.financie.persistence.entity.UserEntity;
import com.github.msemitkin.financie.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public long getOrCreateUserByTelegramId(long telegramId) {
        UserEntity user = userRepository.getUserEntityByTelegramId(telegramId);
        return Optional.ofNullable(user)
            .orElseGet(() -> userRepository.save(new UserEntity(null, telegramId)))
            .getId();
    }
}
