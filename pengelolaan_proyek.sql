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

-- Create PROJECTS table
CREATE TABLE IF NOT EXISTS PROJECTS (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    start_date DATE,
    deadline DATE
);

-- Create TASKS table
CREATE TABLE IF NOT EXISTS TASKS (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    assignee_id INT,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(50) DEFAULT 'TASK',
    status VARCHAR(50) NOT NULL,
    due_date DATE,
    submission_link VARCHAR(255),
    description TEXT DEFAULT NULL,
    notes TEXT DEFAULT NULL,
    FOREIGN KEY (project_id) REFERENCES PROJECTS(id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES USERS(id) ON DELETE SET NULL
);

