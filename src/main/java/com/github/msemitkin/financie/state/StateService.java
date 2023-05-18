package com.github.msemitkin.financie.state;

import com.github.msemitkin.financie.persistence.entity.StateHistoryEntity;
import com.github.msemitkin.financie.persistence.repository.StateRepository;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class StateService {
    private final StateRepository stateRepository;
    private final Gson gson;

    public StateService(StateRepository stateRepository, Gson gson) {
        this.stateRepository = stateRepository;
        this.gson = gson;
    }

    public StateType getStateType(Long userId) {
        return Optional.ofNullable(stateRepository.getStateType(userId))
            .orElse(StateType.NONE);
    }

    public void setStateType(Long userId, StateType stateType) {
        stateRepository.save(new StateHistoryEntity(null, userId, stateType, LocalDateTime.now(), null));
    }

    public <T> void setState(Long userId, StateType stateType, T context) {
        var entity = new StateHistoryEntity(null, userId, stateType, LocalDateTime.now(), gson.toJson(context));
        stateRepository.save(entity);
    }

    public <T> UserState<T> getCurrentState(long userId, Class<T> contextClass) {
        StateHistoryEntity entity = stateRepository.getState(userId);
        if (entity == null) {
            return null;
        } else {
            T context = gson.fromJson(entity.getContext(), contextClass);
            return new UserState<>(entity.getUserId(), entity.getStateType(), context);
        }
    }
}
