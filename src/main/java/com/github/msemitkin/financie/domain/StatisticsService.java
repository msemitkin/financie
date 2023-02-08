package com.github.msemitkin.financie.domain;

import com.github.msemitkin.financie.persistence.entity.TransactionEntity;
import com.github.msemitkin.financie.persistence.repository.CategoryRepository;
import com.github.msemitkin.financie.persistence.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

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

    public Statistics getStatistics(long userId, String category) {
        LocalDateTime from = YearMonth.now().atDay(1).atTime(0, 0);
        LocalDateTime to = LocalDateTime.now();
        Long categoryId = categoryRepository.getCategoryEntityByName(category);
        List<TransactionEntity> allTransactions = transactionRepository
            .findAllByUserIdAndDateTimeBetween(userId, from, to);
        List<TransactionEntity> transactionsInCategory = transactionRepository
            .findAllByUserIdAndCategoryIdAndDateTimeBetween(userId, categoryId, from, to);
        return new Statistics(sum(allTransactions), sum(transactionsInCategory));

    }

    private Double sum(List<TransactionEntity> transactions) {
        return transactions.stream()
            .reduce(0.0, (total, transaction) -> total + transaction.getAmount(), Double::sum);
    }
}
