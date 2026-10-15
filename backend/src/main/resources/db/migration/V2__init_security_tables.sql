-- ==============================================================================
-- Flyway Migration V2: Security & Authorization Tables
-- ==============================================================================

-- 1. Roles
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 2. User Accounts
CREATE TABLE user_accounts (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. User Roles (Many-to-Many Join Table)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 4. Refresh Tokens
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Security Lookups
CREATE INDEX idx_user_accounts_username ON user_accounts(username);
CREATE INDEX idx_user_accounts_email ON user_accounts(email);
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

-- Seed System Roles
INSERT INTO roles (name, description) VALUES
('ROLE_HR_ADMIN', 'HR Administrator with full system, compensation, and user management authority'),
('ROLE_HR_MANAGER', 'HR Manager authorized to manage employees and execute salary revisions'),
('ROLE_HR_VIEWER', 'HR Read-Only Viewer with access to directories, dashboards, and reports');

-- Seed Standard Test Accounts (Pass: Admin@Acme2026!, Manager@Acme2026!, Viewer@Acme2026!)
INSERT INTO user_accounts (username, email, password_hash, is_active) VALUES
('admin', 'admin@acme.corp', '$2a$12$phqW57Ct3Qr2.UcB9AhGZevBOH6UpYtBNSJ67ounPlSZUPjjL3D9y', TRUE),
('manager', 'manager@acme.corp', '$2a$12$Q3jVbIruqaa2p9jXEdijT.ga13yKMM24x4ny22Utb0iqYPqVlHCGK', TRUE),
('viewer', 'viewer@acme.corp', '$2a$12$n4hKTG1ynKRXs1Tj04YVqeHF1.NQkS1/0d4/unNj73eEuYNQP0Y1y', TRUE);

-- Assign Roles
-- Admin gets ROLE_HR_ADMIN
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
-- Manager gets ROLE_HR_MANAGER
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2);
-- Viewer gets ROLE_HR_VIEWER
INSERT INTO user_roles (user_id, role_id) VALUES (3, 3);
