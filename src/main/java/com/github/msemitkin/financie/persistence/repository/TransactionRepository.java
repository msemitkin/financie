package com.github.msemitkin.financie.persistence.repository;

import com.github.msemitkin.financie.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
}
