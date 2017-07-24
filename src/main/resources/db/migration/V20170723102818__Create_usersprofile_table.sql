CREATE TABLE IF NOT EXISTS users_profile (
    username VARCHAR(200) UNIQUE REFERENCES users(username) ON DELETE CASCADE ON UPDATE CASCADE,
    first_name VARCHAR(200),
    last_name VARCHAR(200),
    picture_url VARCHAR(500)
);