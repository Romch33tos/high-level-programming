DROP TABLE IF EXISTS access_rules;
DROP TABLE IF EXISTS resources;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    login VARCHAR(50) PRIMARY KEY,
    salt_hex VARCHAR(128) NOT NULL,
    password_hash_hex VARCHAR(128) NOT NULL
);

CREATE TABLE resources (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_path VARCHAR(255) NOT NULL,
    max_volume INT NOT NULL
);
