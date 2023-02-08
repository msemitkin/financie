package com.github.msemitkin.financie.persistence.repository;

import com.github.msemitkin.financie.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findAllByUserIdAndCategoryIdAndDateTimeBetween(
        Long userId, Long categoryId, LocalDateTime start, LocalDateTime end);

    List<TransactionEntity> findAllByUserIdAndDateTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
