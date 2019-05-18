CREATE USER admin WITH PASSWORD 'asdf1234';
CREATE DATABASE restapi;
ALTER ROLE admin SET client_encoding TO 'utf8';
ALTER ROLE admin SET default_transaction_isolation TO 'read committed';
ALTER ROLE admin SET timezone TO 'UTC';
GRANT ALL PRIVILEGES ON DATABASE restapi TO admin;
