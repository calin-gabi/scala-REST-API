CREATE TABLE IF NOT EXISTS users_oauth (
    username VARCHAR(200) REFERENCES users(username) ON DELETE CASCADE ON UPDATE CASCADE,
    oauth_id VARCHAR(500),
    oauth_type VARCHAR(200),
    UNIQUE(username, oauth_id) INITIALLY DEFERRED
);