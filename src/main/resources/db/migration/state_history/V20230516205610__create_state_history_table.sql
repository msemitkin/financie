CREATE TABLE state_history
(
    id         BIGSERIAL NOT NULL PRIMARY KEY,
    user_id    BIGINT    NOT NULL REFERENCES "user" (id),
    state_type VARCHAR   NOT NULL,
    time       TIMESTAMP NOT NULL
)
