package com.github.msemitkin.financie.telegram.callback.persistence;

import com.github.msemitkin.financie.telegram.callback.CallbackType;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CallbackRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CallbackRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = namedParameterJdbcTemplate;
    }

    public void save(CallbackEntity callback) {
        jdbcTemplate.update("""
                INSERT INTO callback(id, type, payload)
                VALUES (:id, :type, (SELECT to_json(:payload::json)))
                """,
            Map.of(
                "id", callback.id(),
                "type", callback.type().name(),
                "payload", callback.payload()
            ));
    }

    public Optional<CallbackEntity> findById(UUID id) {
        CallbackEntity callback = jdbcTemplate.queryForObject(
            """
                    SELECT * FROM callback
                    WHERE callback.id = (SELECT :id::uuid)
                """,
            Map.of("id", id.toString()),
            (rs, rowNum) -> new CallbackEntity(
                UUID.fromString(rs.getString("id")),
                CallbackType.valueOf(rs.getString("type")),
                rs.getString("payload")
            ));
        return Optional.ofNullable(callback);
    }
}
