ALTER TABLE "user"
    ADD COLUMN telegram_chat_id  BIGINT,
    ADD COLUMN telegram_username VARCHAR(64),
    ADD COLUMN first_name        VARCHAR(64),
    ADD COLUMN last_name         VARCHAR(64)
