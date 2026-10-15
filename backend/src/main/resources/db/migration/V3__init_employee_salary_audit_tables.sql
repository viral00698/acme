-- ==============================================================================
-- Flyway Migration V3: Employees, Effective-Dated Salaries & Audit Tables
-- ==============================================================================

-- 1. Employees Table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_code VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    department_id BIGINT NOT NULL REFERENCES departments(id),
    country_id BIGINT NOT NULL REFERENCES countries(id),
    job_title_id BIGINT NOT NULL REFERENCES job_titles(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' 
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'TERMINATED', 'ON_LEAVE')),
    hire_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Salary Records Table (Effective-Dated)
CREATE TABLE salary_records (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    amount NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    currency_code VARCHAR(3) NOT NULL REFERENCES currencies(code),
    pay_frequency VARCHAR(20) NOT NULL DEFAULT 'ANNUAL'
        CHECK (pay_frequency IN ('ANNUAL', 'MONTHLY', 'BIWEEKLY')),
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_by_id BIGINT REFERENCES user_accounts(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_salary_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

-- Partial Unique Index: Exactly one active salary per employee
CREATE UNIQUE INDEX uq_employee_active_salary 
ON salary_records(employee_id) 
WHERE effective_to IS NULL;

-- 3. Audit Logs Table (Append-Only)
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_username VARCHAR(50) NOT NULL,
    entity_name VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    details_json TEXT NOT NULL,
    client_ip VARCHAR(45),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Performance Indexes for 10,000 Employees Workload
CREATE INDEX idx_employees_code ON employees(employee_code);
CREATE INDEX idx_employees_email ON employees(email);
CREATE INDEX idx_employees_dept_country_status ON employees(department_id, country_id, status);
CREATE INDEX idx_employees_names ON employees(last_name, first_name);
CREATE INDEX idx_employees_hire_date ON employees(hire_date);

-- Indexes for Salary Records
CREATE INDEX idx_salary_employee_history ON salary_records(employee_id, effective_from, effective_to);
CREATE INDEX idx_salary_currency_active ON salary_records(currency_code) WHERE effective_to IS NULL;
CREATE INDEX idx_salary_effective_from ON salary_records(effective_from);

-- Indexes for Audit Inquiries
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_username);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
