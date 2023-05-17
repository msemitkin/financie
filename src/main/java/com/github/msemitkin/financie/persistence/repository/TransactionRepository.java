package com.github.msemitkin.financie.persistence.repository;

import com.github.msemitkin.financie.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    @Query("""
        SELECT t FROM TransactionEntity t
        WHERE t.userId = :userId
        AND t.categoryId = :categoryId
        AND t.dateTime >= :start
        AND t.dateTime < :end""")
    List<TransactionEntity> findAllByUserIdAndCategoryIdAndDateTimeBetween(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT t FROM TransactionEntity t
        WHERE t.userId = :userId
        AND t.dateTime >= :start
        AND t.dateTime < :end""")
    List<TransactionEntity> findAllByUserIdAndDateTimeBetween(
        @Param("userId") Long userId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
