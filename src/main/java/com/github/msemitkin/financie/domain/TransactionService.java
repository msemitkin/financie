package com.github.msemitkin.financie.domain;

import com.github.msemitkin.financie.persistence.entity.CategoryEntity;
import com.github.msemitkin.financie.persistence.entity.TransactionEntity;
import com.github.msemitkin.financie.persistence.entity.UserEntity;
import com.github.msemitkin.financie.persistence.repository.CategoryRepository;
import com.github.msemitkin.financie.persistence.repository.TransactionRepository;
import com.github.msemitkin.financie.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionService(
        TransactionRepository transactionRepository,
        CategoryRepository categoryRepository,
        UserRepository userRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public long getOrCreateUserByTelegramId(long telegramId) {
        UserEntity user = userRepository.getUserEntityByTelegramId(telegramId);
        return Optional.ofNullable(user)
            .orElseGet(() -> userRepository.save(new UserEntity(null, telegramId)))
            .getId();
    }

    public void saveTransaction(SaveTransactionCommand command) {
        Long categoryId = getOrCreateCategoryId(command.category());
        TransactionEntity transaction = new TransactionEntity(
            null, command.userId(), command.amount(), categoryId, command.description());
        transactionRepository.save(transaction);
    }

    long getOrCreateCategoryId(String name) {
        Long categoryId = categoryRepository.getCategoryEntityByName(name);
        return Optional.ofNullable(categoryId)
            .orElseGet(() -> categoryRepository.save(new CategoryEntity(null, name)).getId());
    }
}