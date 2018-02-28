CREATE TABLE users (
  id       BIGSERIAL PRIMARY KEY,
  username VARCHAR NOT NULL,
  fullname VARCHAR NOT NULL,
  password VARCHAR,
  role VARCHAR (200),
  last_login TIMESTAMP,
  attempts INT DEFAULT 0,
  lockoutDate TIMESTAMP,
  twoFactor BOOLEAN DEFAULT FALSE,
  email VARCHAR (200),
  emailConfirmed BOOLEAN DEFAULT FALSE,
  phone VARCHAR (200),
  phoneConfirmed BOOLEAN DEFAULT FALSE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created TIMESTAMP default now(),
  rev INT NOT NULL DEFAULT 0
);
