CREATE TABLE callback
(
    id      UUID    NOT NULL PRIMARY KEY,
    type    VARCHAR NOT NULL,
    payload JSON    NOT NULL
);