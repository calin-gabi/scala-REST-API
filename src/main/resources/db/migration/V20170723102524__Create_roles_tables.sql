CREATE TABLE IF NOT EXISTS roles (
  role VARCHAR(100) PRIMARY KEY,
  description VARCHAR(500) DEFAULT '',
  rev INT NOT NULL DEFAULT 0
);
