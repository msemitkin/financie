ALTER TABLE transaction
    ADD FOREIGN KEY (user_id) REFERENCES "user" (id);
CREATE INDEX transaction_user_id_idx ON transaction (user_id);
