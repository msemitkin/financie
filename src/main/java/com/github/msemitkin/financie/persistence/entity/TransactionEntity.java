package com.github.msemitkin.financie.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "category_id")
    private Long categoryId;
    @Column(name = "description")
    private String description;
    @Column(name = "date_time")
    private LocalDateTime dateTime;

    public TransactionEntity() {
    }

    public TransactionEntity(
        Long id,
        Long userId,
        Double amount,
        Long categoryId,
        String description,
        LocalDateTime dateTime
    ) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.categoryId = categoryId;
        this.description = description;
        this.dateTime = dateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime time) {
        this.dateTime = time;
    }
}
