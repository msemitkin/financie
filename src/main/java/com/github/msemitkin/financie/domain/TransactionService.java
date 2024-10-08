package com.github.msemitkin.financie.domain;

import com.github.msemitkin.financie.persistence.entity.CategoryEntity;
import com.github.msemitkin.financie.persistence.entity.TransactionEntity;
import com.github.msemitkin.financie.persistence.mapper.TransactionMapper;
import com.github.msemitkin.financie.persistence.repository.CategoryRepository;
import com.github.msemitkin.financie.persistence.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
        LocalDateTime transactionTime = requireNonNullElse(command.utcDateTime(), LocalDateTime.now());
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
            .map(this::toTransaction)
            .orElseThrow();
    }

    public List<Transaction> getTransactions(long userId) {
        return transactionRepository.findAllByUserIdOrderByDateTimeDesc(userId)
            .stream().map(this::toTransaction).toList();
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

    public List<Transaction> getTransactions(
        long userId,
        OffsetDateTime from,
        OffsetDateTime to
    ) {
        List<TransactionEntity> transactions = transactionRepository
            .findAllByUserIdAndDateTimeBetween(userId, from.toLocalDateTime(), to.toLocalDateTime());

        return transactions.stream()
            .map(tran -> categoryRepository
                .findById(tran.getCategoryId())
                .map(CategoryEntity::getName)
                .map(categoryName -> TransactionMapper.toTransaction(tran, categoryName))
                .orElseThrow()).toList();
    }

    public void deleteTransaction(long transactionId) {
        transactionRepository.deleteById(transactionId);
    }

    private long getOrCreateCategoryId(String name) {
        Long categoryId = categoryRepository.getCategoryEntityByName(name);
        return Optional.ofNullable(categoryId)
            .orElseGet(() -> categoryRepository.save(new CategoryEntity(null, name)).getId());
    }

    @NonNull
    private Transaction toTransaction(TransactionEntity tran) {
        Long categoryId = tran.getCategoryId();
        String categoryName = categoryRepository.findById(categoryId)
            .map(CategoryEntity::getName)
            .orElse(null);
        return TransactionMapper.toTransaction(tran, categoryName);
    }
}
