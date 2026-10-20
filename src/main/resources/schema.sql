-- ============================================================================
-- Online Personal Finance Management Platform - Database Schema & Seed Data
-- ============================================================================

CREATE DATABASE IF NOT EXISTS personal_finance_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE personal_finance_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
) ENGINE=InnoDB;

-- 2. Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_category UNIQUE (user_id, name, type),
    INDEX idx_category_user (user_id)
) ENGINE=InnoDB;

-- 3. Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    description VARCHAR(255) NOT NULL,
    txn_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) 
        REFERENCES categories(id) ON DELETE RESTRICT,
    INDEX idx_txn_user_date (user_id, txn_date),
    INDEX idx_txn_category (category_id)
) ENGINE=InnoDB;

-- 4. Budgets Table
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    limit_amount DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) 
        REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_budget_period UNIQUE (user_id, category_id, month, year),
    INDEX idx_budget_user_period (user_id, year, month)
) ENGINE=InnoDB;

-- 5. Goals Table
CREATE TABLE IF NOT EXISTS goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    target_amount DECIMAL(12, 2) NOT NULL,
    saved_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    deadline DATE NOT NULL,
    status ENUM('IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_goals_user_status (user_id, status)
) ENGINE=InnoDB;

-- ============================================================================
-- Seed Data (Realistic sample data for demonstration)
-- Demo login credentials: alex@financeapp.com / Password123
-- ============================================================================

INSERT INTO users (id, name, email, password_hash)
VALUES (1, 'Alex Morgan', 'alex@financeapp.com', '$2a$10$Cvyw8VT95SX7u8oyYVOooey.QMJbWGHFVYJfCHZlOEuhbwr.X43Je')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Income Categories
INSERT INTO categories (id, user_id, name, type) VALUES
(1, 1, 'Salary', 'INCOME'),
(2, 1, 'Freelance', 'INCOME'),
(3, 1, 'Investments', 'INCOME')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Expense Categories
INSERT INTO categories (id, user_id, name, type) VALUES
(4, 1, 'Housing & Rent', 'EXPENSE'),
(5, 1, 'Groceries & Dining', 'EXPENSE'),
(6, 1, 'Utilities & Bills', 'EXPENSE'),
(7, 1, 'Transportation', 'EXPENSE'),
(8, 1, 'Entertainment & Leisure', 'EXPENSE'),
(9, 1, 'Healthcare & Medical', 'EXPENSE')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Monthly Budgets (Current demonstration month)
-- Month 10, Year 2026
INSERT INTO budgets (id, user_id, category_id, month, year, limit_amount) VALUES
(1, 1, 4, 10, 2026, 1500.00),
(2, 1, 5, 10, 2026, 600.00),
(3, 1, 6, 10, 2026, 250.00),
(4, 1, 7, 10, 2026, 300.00),
(5, 1, 8, 10, 2026, 200.00)
ON DUPLICATE KEY UPDATE limit_amount = VALUES(limit_amount);

-- Sample Transactions for October 2026
INSERT INTO transactions (id, user_id, category_id, amount, type, description, txn_date) VALUES
(1, 1, 1, 5000.00, 'INCOME', 'Monthly Primary Salary', '2026-10-01'),
(2, 1, 2, 850.00, 'INCOME', 'UI Design Contract Work', '2026-10-12'),
(3, 1, 4, 1400.00, 'EXPENSE', 'Apartment Monthly Rent', '2026-10-02'),
(4, 1, 5, 520.00, 'EXPENSE', 'Weekly Supermarket Runs', '2026-10-08'),
(5, 1, 6, 210.00, 'EXPENSE', 'Electricity and High-Speed Fiber', '2026-10-05'),
(6, 1, 7, 180.00, 'EXPENSE', 'Monthly Metro Pass and Fuel', '2026-10-07'),
(7, 1, 8, 175.00, 'EXPENSE', 'Concert Tickets and Movie Night', '2026-10-14')
ON DUPLICATE KEY UPDATE amount = VALUES(amount);

-- Sample Savings Goals
INSERT INTO goals (id, user_id, title, target_amount, saved_amount, deadline, status) VALUES
(1, 1, 'Emergency Fund (6 Months)', 10000.00, 7500.00, '2026-12-31', 'IN_PROGRESS'),
(2, 1, 'New Workstation Setup', 2200.00, 2200.00, '2026-10-15', 'COMPLETED'),
(3, 1, 'Japan Holiday Trip', 4500.00, 1800.00, '2027-05-30', 'IN_PROGRESS')
ON DUPLICATE KEY UPDATE saved_amount = VALUES(saved_amount);

