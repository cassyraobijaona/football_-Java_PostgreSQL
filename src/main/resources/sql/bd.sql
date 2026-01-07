CREATE DATABASE mini_football_db;
CREATE USER mini_football_db_manager WITH PASSWORD 'malalatiana';

\c mini_football_db;

GRANT CONNECT ON DATABASE mini_football_db TO mini_football_db_manager;
GRANT USAGE ON SCHEMA public TO mini_football_db_manager;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE Team TO mini_football_db_manager;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE Player TO mini_football_db_manager;
