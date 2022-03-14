CREATE TABLE account (
    id SERIAL PRIMARY KEY,
    username VARCHAR NOT NULL,
    phone VARCHAR NOT NULL UNIQUE
);

CREATE TABLE ticket (
    id SERIAL PRIMARY KEY,
    session_id INT NOT NULL,
    row INT NOT NULL,
    cell INT NOT NULL,
    account_id INT NOT NULL REFERENCES account(id)
);

ALTER TABLE ticket
    ADD CONSTRAINT unique_ticket UNIQUE (session_id, row, cell);

ALTER TABLE ticket
    add constraint ckRow check (row in (1, 2, 3));

ALTER TABLE ticket
    add constraint ckCell check (cell in (1, 2, 3));