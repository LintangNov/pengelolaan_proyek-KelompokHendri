-- Create database if not exists
CREATE DATABASE IF NOT EXISTS pengelolaan_proyek;
USE pengelolaan_proyek;

-- Create USERS table
CREATE TABLE IF NOT EXISTS USERS (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- Truncate/clean existing users for a fresh setup (optional, but good for demo)
TRUNCATE TABLE USERS;

-- Insert sample users with roles PM, DEV, UIUX
INSERT INTO USERS (username, password, role) VALUES 
('hendri_pm', 'password123', 'PM'),
('lintang_dev', 'devpass456', 'DEV'),
('nov_uiux', 'design789', 'UIUX');
