package com.github.msemitkin.financie.persistence.repository;

import com.github.msemitkin.financie.persistence.entity.UserEntity;
import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Nullable
    UserEntity getUserEntityByTelegramId(Long telegramId);
}
