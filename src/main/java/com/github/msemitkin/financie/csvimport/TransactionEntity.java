package com.github.msemitkin.financie.csvimport;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import java.time.LocalDateTime;

public class TransactionEntity {
    @CsvBindByName(column = "amount", required = true)
    private Double amount;
    @CsvBindByName(column = "category", required = true)
    private String category;
    @CsvDate("yyyy-MM-dd HH:mm")
    @CsvBindByName(column = "datetime", required = true)
    private LocalDateTime transactionTime;
    @CsvBindByName(column = "description")
    private String description;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
