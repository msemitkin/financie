package com.github.msemitkin.financie.domain;

import com.github.msemitkin.financie.persistence.entity.CategoryEntity;
import com.github.msemitkin.financie.persistence.entity.TransactionEntity;
import com.github.msemitkin.financie.persistence.mapper.TransactionMapper;
import com.github.msemitkin.financie.persistence.repository.CategoryRepository;
import com.github.msemitkin.financie.persistence.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNullElse;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionValidator transactionValidator;

    public TransactionService(
        TransactionRepository transactionRepository,
        CategoryRepository categoryRepository,
        TransactionValidator transactionValidator
    ) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.transactionValidator = transactionValidator;
    }

    public void saveTransaction(SaveTransactionCommand command) {
        transactionValidator.validateTransaction(command);

        Long categoryId = getOrCreateCategoryId(command.category());
        LocalDateTime transactionTime = requireNonNullElse(command.dateTime(), LocalDateTime.now());
        TransactionEntity transaction = new TransactionEntity(
            null, command.userId(), command.amount(), categoryId, command.description(), transactionTime);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void saveTransactions(List<SaveTransactionCommand> commands) {
        commands.forEach(this::saveTransaction);
    }

    public Transaction getTransaction(long id) {
        return transactionRepository.findById(id)
            .map(tran -> {
                Long categoryId = tran.getCategoryId();
                String categoryName = categoryRepository.findById(categoryId)
                    .map(CategoryEntity::getName)
                    .orElse(null);
                return new Transaction(
                    tran.getId(),
                    tran.getUserId(),
                    tran.getAmount(),
                    categoryName,
                    tran.getDescription(),
                    tran.getDateTime()
                );
            })
            .orElseThrow();
    }

    public List<Transaction> getTransactions(
        long userId,
        String category,
        LocalDateTime from,
        LocalDateTime to
    ) {
        Long categoryId = categoryRepository.getCategoryEntityByName(category);
        List<TransactionEntity> transactions = transactionRepository
            .findAllByUserIdAndCategoryIdAndDateTimeBetween(userId, categoryId, from, to);

        return transactions.stream().map(tran -> TransactionMapper.toTransaction(tran, category)).toList();
    }

    public void deleteTransaction(long transactionId) {
        transactionRepository.deleteById(transactionId);
    }

    private long getOrCreateCategoryId(String name) {
        Long categoryId = categoryRepository.getCategoryEntityByName(name);
        return Optional.ofNullable(categoryId)
            .orElseGet(() -> categoryRepository.save(new CategoryEntity(null, name)).getId());
    }
}
