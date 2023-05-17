package com.github.msemitkin.financie.persistence.repository;

import com.github.msemitkin.financie.persistence.entity.StateHistoryEntity;
import com.github.msemitkin.financie.state.StateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface StateRepository extends JpaRepository<StateHistoryEntity, Long> {


    @Nullable
    StateHistoryEntity findTopByUserIdOrderByTimeDesc(Long userId);

    @Nullable
    default StateType getStateType(Long userId) {
        return Optional.ofNullable(findTopByUserIdOrderByTimeDesc(userId))
            .map(StateHistoryEntity::getStateType)
            .orElse(null);

    }
}
