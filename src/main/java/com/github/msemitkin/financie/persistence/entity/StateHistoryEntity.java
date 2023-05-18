package com.github.msemitkin.financie.persistence.entity;

import com.github.msemitkin.financie.state.StateType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "state_history")
public class StateHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private StateType stateType;
    private LocalDateTime time;
    private String context;

    public StateHistoryEntity() {
    }

    public StateHistoryEntity(
        Long id,
        Long userId,
        StateType stateType,
        LocalDateTime time,
        String context
    ) {
        this.id = id;
        this.userId = userId;
        this.stateType = stateType;
        this.time = time;
        this.context = context;
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

    public StateType getStateType() {
        return stateType;
    }

    public void setStateType(StateType stateType) {
        this.stateType = stateType;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
