DROP TABLE IF EXISTS access_rules;
DROP TABLE IF EXISTS resources;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    login VARCHAR(50) PRIMARY KEY,
    salt_hex VARCHAR(128) NOT NULL,
    password_hash_hex VARCHAR(128) NOT NULL
);
