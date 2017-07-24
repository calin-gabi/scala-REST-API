CREATE TABLE IF NOT EXISTS users_oauth (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    oauth_id VARCHAR(500),
    oauth_type VARCHAR(200),
    UNIQUE(user_id, oauth_id) INITIALLY DEFERRED
);