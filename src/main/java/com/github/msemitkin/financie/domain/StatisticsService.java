package com.github.msemitkin.financie.domain;

import com.github.msemitkin.financie.persistence.entity.CategoryEntity;
import com.github.msemitkin.financie.persistence.entity.TransactionEntity;
import com.github.msemitkin.financie.persistence.repository.CategoryRepository;
import com.github.msemitkin.financie.persistence.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public StatisticsService(
        TransactionRepository transactionRepository,
        CategoryRepository categoryRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryStatistics> getStatistics(long userId, LocalDateTime from, LocalDateTime to) {
        List<TransactionEntity> transactions = transactionRepository
            .findAllByUserIdAndDateTimeBetween(userId, from, to);
        Map<Long, List<TransactionEntity>> transactionsByCategoryId = transactions.stream()
            .collect(Collectors.groupingBy(TransactionEntity::getCategoryId));
        Map<Long, Double> amounts = transactionsByCategoryId.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> sum(entry.getValue())));
        Map<Long, CategoryEntity> categories = categoryRepository
            .findAllByIds(new ArrayList<>(transactionsByCategoryId.keySet()));
        return amounts.entrySet().stream()
            .map(entry -> new CategoryStatistics(
                    categories.get(entry.getKey()).getName(),
                    entry.getValue()
                )
            )
            .sorted(Comparator.comparingDouble(CategoryStatistics::amount).reversed())
            .toList();
    }

    public Statistics getStatistics(long userId, String category, LocalDateTime from, LocalDateTime to) {
        Long categoryId = categoryRepository.getCategoryEntityByName(category);
        List<TransactionEntity> allTransactions = transactionRepository
            .findAllByUserIdAndDateTimeBetween(userId, from, to);
        List<TransactionEntity> transactionsInCategory = transactionRepository
            .findAllByUserIdAndCategoryIdAndDateTimeBetween(userId, categoryId, from, to);
        return new Statistics(sum(allTransactions), sum(transactionsInCategory));

    }

    public Statistics getDailyStatistics(long userId, String category, LocalDate date) {
        Long categoryId = categoryRepository.getCategoryEntityByName(category);
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        double totalInCategory = getTotalSpent(userId, categoryId, from, to);
        double total = getTotalSpent(userId, from, to);
        return new Statistics(total, totalInCategory);
    }

    private double getTotalSpent(long userId, long categoryId, LocalDateTime from, LocalDateTime to) {
        List<TransactionEntity> transactions = transactionRepository
            .findAllByUserIdAndCategoryIdAndDateTimeBetween(userId, categoryId, from, to);
        return sum(transactions);
    }

    private double getTotalSpent(long userId, LocalDateTime from, LocalDateTime to) {
        List<TransactionEntity> transactions = transactionRepository
            .findAllByUserIdAndDateTimeBetween(userId, from, to);
        return sum(transactions);
    }

    private Double sum(Collection<TransactionEntity> transactions) {
        return transactions.stream()
            .reduce(0.0, (total, transaction) -> total + transaction.getAmount(), Double::sum);
    }
}
