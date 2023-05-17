package com.github.msemitkin.financie.state;

import com.github.msemitkin.financie.persistence.entity.StateHistoryEntity;
import com.github.msemitkin.financie.persistence.repository.StateRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class StateService {
    private final StateRepository stateRepository;

    public StateService(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    public StateType getStateType(Long userId) {
        return Optional.ofNullable(stateRepository.getStateType(userId))
            .orElse(StateType.NONE);
    }

    public void setStateType(Long userId, StateType stateType) {
        stateRepository.save(new StateHistoryEntity(null, userId, stateType, LocalDateTime.now()));
    }
}
