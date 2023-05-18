package com.github.msemitkin.financie.persistence.repository;

import com.github.msemitkin.financie.persistence.entity.StateHistoryEntity;
import com.github.msemitkin.financie.state.StateType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class StateRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public StateRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Nullable
    public StateType getStateType(Long userId) {
        return Optional.ofNullable(getState(userId))
            .map(StateHistoryEntity::getStateType)
            .orElse(null);

    }

    public StateHistoryEntity getState(Long userId) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT * FROM state_history WHERE user_id = :userId ORDER BY time DESC LIMIT 1",
                Map.of("userId", userId),
                (rs, rowNum) -> new StateHistoryEntity(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    StateType.byId(Integer.parseInt(rs.getString("state_type"))),
                    rs.getTimestamp("time").toLocalDateTime(),
                    rs.getString("context")
                ));
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    public void save(StateHistoryEntity entity) {
        if (entity.getContext() != null) {
            jdbcTemplate.update("""
                    INSERT INTO state_history (user_id, state_type, "time", context)
                    VALUES (:userId, :stateType, :time, (SELECT to_json(:context::json)))""",
                Map.of(
                    "userId", entity.getUserId(),
                    "stateType", entity.getStateType().getId(),
                    "time", entity.getTime(),
                    "context", entity.getContext()
                ));
        } else {
            jdbcTemplate.update("""
                    INSERT INTO state_history (user_id, state_type, "time")
                    VALUES (:userId, :stateType, :time)""",
                Map.of(
                    "userId", entity.getUserId(),
                    "stateType", entity.getStateType().getId(),
                    "time", entity.getTime()
                ));
        }
    }
}
