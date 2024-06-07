ALTER TABLE transaction
    ADD FOREIGN KEY (category_id) REFERENCES category (id);
CREATE INDEX transaction_category_id_idx ON transaction (category_id);
