-- Test Data for H2
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_HR_ADMIN', 'HR Administrator'),
(2, 'ROLE_HR_MANAGER', 'HR Manager'),
(3, 'ROLE_HR_VIEWER', 'HR Viewer');

INSERT INTO user_accounts (id, username, email, password_hash, is_active, created_at, updated_at) VALUES
(1, 'admin', 'admin@acme.corp', '$2a$12$phqW57Ct3Qr2.UcB9AhGZevBOH6UpYtBNSJ67ounPlSZUPjjL3D9y', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'manager', 'manager@acme.corp', '$2a$12$Q3jVbIruqaa2p9jXEdijT.ga13yKMM24x4ny22Utb0iqYPqVlHCGK', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'viewer', 'viewer@acme.corp', '$2a$12$n4hKTG1ynKRXs1Tj04YVqeHF1.NQkS1/0d4/unNj73eEuYNQP0Y1y', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3);

INSERT INTO currencies (code, name, symbol) VALUES
('USD', 'US Dollar', '$'),
('EUR', 'Euro', '€'),
('GBP', 'British Pound', '£');

INSERT INTO countries (id, code, name, default_currency_code) VALUES
(1, 'USA', 'United States', 'USD'),
(2, 'GBR', 'United Kingdom', 'GBP');

INSERT INTO departments (id, code, name) VALUES
(1, 'ENG', 'Engineering'),
(2, 'HR', 'Human Resources');

INSERT INTO job_titles (id, title, department_id) VALUES
(1, 'Software Engineer', 1),
(2, 'HR Specialist', 2);

INSERT INTO employees (employee_code, first_name, last_name, email, phone_number, department_id, country_id, job_title_id, status, hire_date, created_at, updated_at) VALUES
('EMP-00001', 'John', 'Doe', 'john.doe@acme.corp', '+1-555-0100', 1, 1, 1, 'ACTIVE', '2023-01-10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
