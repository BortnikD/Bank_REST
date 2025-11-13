--liquibase formatted sql

--changeset BortnikD:create-users-and-cards-tables
CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    role       VARCHAR(20)  NOT NULL,
    username   VARCHAR(64)  NOT NULL UNIQUE,
    password   VARCHAR(256) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE cards
(
    id              UUID PRIMARY KEY,
    user_id         UUID REFERENCES users (id) ON DELETE CASCADE NOT NULL,
    card_number     VARCHAR(16)                                  NOT NULL UNIQUE,
    status          VARCHAR(20)                                  NOT NULL,
    expiration_date DATE                                         NOT NULL,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_role ON users (role);

CREATE INDEX idx_cards_user_id ON cards (user_id);
CREATE INDEX idx_cards_status ON cards (status);
CREATE INDEX idx_cards_number ON cards (card_number);
