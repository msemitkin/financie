CREATE TABLE transaction
(
    id          BIGSERIAL,
    user_id     BIGINT,
    amount      DOUBLE PRECISION,
    category_id BIGINT,
    description TEXT,
    date_time   TIMESTAMP
);