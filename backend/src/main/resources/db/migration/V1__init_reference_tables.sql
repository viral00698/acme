-- ==============================================================================
-- Flyway Migration V1: Reference Tables
-- ==============================================================================

-- 1. Currencies (ISO 4217)
CREATE TABLE currencies (
    code VARCHAR(3) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(5) NOT NULL
);

-- 2. Countries (ISO 3166-1 alpha-3)
CREATE TABLE countries (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    default_currency_code VARCHAR(3) NOT NULL REFERENCES currencies(code)
);

-- 3. Departments
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

-- 4. Job Titles
CREATE TABLE job_titles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    department_id BIGINT NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    CONSTRAINT uq_job_title_dept UNIQUE (title, department_id)
);

-- Seed Currencies
INSERT INTO currencies (code, name, symbol) VALUES
('USD', 'United States Dollar', '$'),
('EUR', 'Euro', '€'),
('GBP', 'British Pound', '£'),
('INR', 'Indian Rupee', '₹'),
('JPY', 'Japanese Yen', '¥'),
('CAD', 'Canadian Dollar', '$'),
('AUD', 'Australian Dollar', '$'),
('SGD', 'Singapore Dollar', '$');

-- Seed Countries
INSERT INTO countries (code, name, default_currency_code) VALUES
('USA', 'United States', 'USD'),
('GBR', 'United Kingdom', 'GBP'),
('DEU', 'Germany', 'EUR'),
('IND', 'India', 'INR'),
('JPN', 'Japan', 'JPY'),
('CAN', 'Canada', 'CAD'),
('AUS', 'Australia', 'AUD'),
('SGP', 'Singapore', 'SGD');

-- Seed Departments
INSERT INTO departments (code, name) VALUES
('ENG', 'Engineering'),
('HR', 'Human Resources'),
('FIN', 'Finance'),
('SAL', 'Sales'),
('MKT', 'Marketing'),
('OPS', 'Operations'),
('LGL', 'Legal'),
('PRD', 'Product Management');

-- Seed Job Titles
INSERT INTO job_titles (title, department_id) VALUES
-- Engineering
('Software Engineer', 1),
('Senior Software Engineer', 1),
('Staff Software Engineer', 1),
('Principal Engineer', 1),
('Engineering Manager', 1),
('QA Automation Engineer', 1),
('DevOps Engineer', 1),
-- Human Resources
('HR Specialist', 2),
('HR Business Partner', 2),
('Talent Acquisition Lead', 2),
('Chief People Officer', 2),
-- Finance
('Financial Analyst', 3),
('Senior Accountant', 3),
('Finance Manager', 3),
('VP of Finance', 3),
-- Sales
('Sales Representative', 4),
('Account Executive', 4),
('Sales Manager', 4),
('VP of Global Sales', 4),
-- Marketing
('Marketing Specialist', 5),
('Product Marketing Manager', 5),
('Growth Marketing Lead', 5),
-- Operations
('Operations Specialist', 6),
('Operations Manager', 6),
-- Legal
('Legal Counsel', 7),
('Compliance Officer', 7),
-- Product
('Associate Product Manager', 8),
('Product Manager', 8),
('Senior Product Manager', 8),
('VP of Product', 8);
