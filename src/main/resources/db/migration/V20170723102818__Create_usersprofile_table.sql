CREATE TABLE IF NOT EXISTS users_profile (
    user_id BIGINT UNIQUE REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    first_name VARCHAR(200),
    last_name VARCHAR(200),
    picture_url VARCHAR(500)
);