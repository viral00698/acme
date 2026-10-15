# Database Design & Schema Specification — ACME Employee Salary Management System

## 1. Relational Model & Normalization (3NF)
The schema is normalized to Third Normal Form (3NF) to guarantee transactional integrity, eliminate data duplication, and prevent anomalies:
1. **1NF**: All column values are atomic; each row is uniquely identifiable by a primary key (`BIGSERIAL` / `BIGINT`).
2. **2NF**: All non-key attributes are fully functionally dependent on the entire primary key.
3. **3NF**: Non-key attributes depend only on the primary key, with zero transitive dependencies. Reference data (Departments, Countries, Currencies, Job Titles) are extracted into distinct lookup tables rather than denormalized as free-text strings on Employee rows.

## 2. Entity-Relationship Overview

```mermaid
erDiagram
    ROLES ||--o{ USER_ROLES : has
    USER_ACCOUNTS ||--o{ USER_ROLES : assigned
    USER_ACCOUNTS ||--o{ REFRESH_TOKENS : owns
    USER_ACCOUNTS ||--o{ AUDIT_LOGS : performs

    COUNTRIES ||--o{ EMPLOYEES : located_in
    DEPARTMENTS ||--o{ EMPLOYEES : works_in
    JOB_TITLES ||--o{ EMPLOYEES : assigned_to

    EMPLOYEES ||--o{ SALARY_RECORDS : compensation_history
    CURRENCIES ||--o{ SALARY_RECORDS : denominated_in

    COUNTRIES {
        bigint id PK
        varchar(3) code UK
        varchar(100) name
        varchar(3) default_currency_code
    }

    DEPARTMENTS {
        bigint id PK
        varchar(50) code UK
        varchar(100) name
    }

    JOB_TITLES {
        bigint id PK
        varchar(100) title UK
        bigint department_id FK
    }

    CURRENCIES {
        varchar(3) code PK
        varchar(50) name
        varchar(5) symbol
    }

    USER_ACCOUNTS {
        bigint id PK
        varchar(50) username UK
        varchar(100) email UK
        varchar(255) password_hash
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }

    ROLES {
        bigint id PK
        varchar(50) name UK
        varchar(255) description
    }

    USER_ROLES {
        bigint user_id PK, FK
        bigint role_id PK, FK
    }

    REFRESH_TOKENS {
        bigint id PK
        bigint user_id FK
        varchar(255) token_hash UK
        timestamp expiry_date
        boolean revoked
        timestamp created_at
    }

    EMPLOYEES {
        bigint id PK
        varchar(20) employee_code UK
        varchar(100) first_name
        varchar(100) last_name
        varchar(150) email UK
        varchar(20) phone_number
        bigint department_id FK
        bigint country_id FK
        bigint job_title_id FK
        varchar(20) status
        date hire_date
        timestamp created_at
        timestamp updated_at
    }

    SALARY_RECORDS {
        bigint id PK
        bigint employee_id FK
        numeric(15,2) amount
        varchar(3) currency_code FK
        varchar(20) pay_frequency
        date effective_from
        date effective_to
        bigint created_by_id FK
        timestamp created_at
        bigint version
    }

    AUDIT_LOGS {
        bigint id PK
        varchar(50) actor_username
        varchar(50) entity_name
        bigint entity_id
        varchar(50) action
        text details_json
        varchar(45) client_ip
        timestamp created_at
    }
```

## 3. Data Dictionary

### 3.1 `countries`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGSERIAL | PRIMARY KEY | Unique identifier |
| `code` | VARCHAR(3) | UNIQUE, NOT NULL | ISO 3166-1 alpha-3 (e.g. USA, GBR, DEU, IND) |
| `name` | VARCHAR(100) | NOT NULL | Full country name |
| `default_currency_code`| VARCHAR(3) | NOT NULL | Default ISO 4217 currency code |

### 3.2 `departments`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGSERIAL | PRIMARY KEY | Unique identifier |
| `code` | VARCHAR(50) | UNIQUE, NOT NULL | Department code (e.g. ENG, HR, FIN, SAL, MKT) |
| `name` | VARCHAR(100) | NOT NULL | Department name |

### 3.3 `job_titles`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGSERIAL | PRIMARY KEY | Unique identifier |
| `title` | VARCHAR(100) | NOT NULL | Job role title (e.g. Senior Software Engineer) |
| `department_id` | BIGINT | REFERENCES departments(id) | Department relationship |

### 3.4 `currencies`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `code` | VARCHAR(3) | PRIMARY KEY | ISO 4217 code (e.g. USD, EUR, GBP, INR) |
| `name` | VARCHAR(50) | NOT NULL | Currency name |
| `symbol` | VARCHAR(5) | NOT NULL | Currency symbol ($, €, £, ₹) |

### 3.5 `user_accounts` & `roles`
- `user_accounts`: Stores administrative users (`id`, `username`, `email`, `password_hash`, `is_active`, `created_at`, `updated_at`).
- `roles`: Pre-seeded with `ROLE_HR_ADMIN`, `ROLE_HR_MANAGER`, `ROLE_HR_VIEWER`.
- `user_roles`: Composite primary key `(user_id, role_id)`.

### 3.6 `employees`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGSERIAL | PRIMARY KEY | Surrogate identifier |
| `employee_code` | VARCHAR(20) | UNIQUE, NOT NULL | Business key (e.g. EMP-00001 to EMP-10000) |
| `first_name` | VARCHAR(100) | NOT NULL | First name |
| `last_name` | VARCHAR(100) | NOT NULL | Last name |
| `email` | VARCHAR(150) | UNIQUE, NOT NULL | Synthetic corporate email (`user@acme.corp`) |
| `phone_number` | VARCHAR(20) | NULL | Work telephone |
| `department_id` | BIGINT | FK -> departments(id) | Current assigned department |
| `country_id` | BIGINT | FK -> countries(id) | Employment jurisdiction |
| `job_title_id` | BIGINT | FK -> job_titles(id) | Assigned job title |
| `status` | VARCHAR(20) | NOT NULL | `ACTIVE`, `INACTIVE`, `TERMINATED`, `ON_LEAVE` |
| `hire_date` | DATE | NOT NULL | Initial employment date |
| `created_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | Record creation timestamp |
| `updated_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | Last modified timestamp |

### 3.7 `salary_records`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGSERIAL | PRIMARY KEY | Surrogate identifier |
| `employee_id` | BIGINT | FK -> employees(id) | Associated employee |
| `amount` | NUMERIC(15,2)| NOT NULL, CHECK (amount > 0) | Exact compensation amount |
| `currency_code`| VARCHAR(3) | FK -> currencies(code) | ISO currency code |
| `pay_frequency`| VARCHAR(20) | NOT NULL | `ANNUAL`, `MONTHLY`, `BIWEEKLY` |
| `effective_from`| DATE | NOT NULL | Start date of salary period |
| `effective_to` | DATE | NULL | End date (NULL signifies active current record) |
| `created_by_id`| BIGINT | FK -> user_accounts(id) | Actor who authorized compensation |
| `created_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | Revision creation timestamp |
| `version` | BIGINT | NOT NULL DEFAULT 0 | Optimistic locking counter |

### 3.8 `audit_logs`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGSERIAL | PRIMARY KEY | Surrogate identifier |
| `actor_username`| VARCHAR(50)| NOT NULL | Username performing action |
| `entity_name` | VARCHAR(50) | NOT NULL | E.g., `EMPLOYEE`, `SALARY_RECORD` |
| `entity_id` | BIGINT | NOT NULL | Target entity primary key |
| `action` | VARCHAR(50) | NOT NULL | `CREATE`, `UPDATE`, `SALARY_REVISION`, `STATUS_CHANGE` |
| `details_json` | TEXT | NOT NULL | JSON payload of before/after state diff |
| `client_ip` | VARCHAR(45) | NULL | IPv4/IPv6 client address |
| `created_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | Timestamp of audit entry |

## 4. Indexing Strategy for 10,000+ Records
To ensure sub-100ms response times for paginated searches and analytics:
1. `idx_employees_code`: B-tree on `employee_code` (Unique).
2. `idx_employees_email`: B-tree on `email` (Unique).
3. `idx_employees_dept_country_status`: Composite index on `(department_id, country_id, status)`.
4. `idx_employees_names`: B-tree on `(last_name, first_name)` for alphabetical directory browsing.
5. `idx_salary_employee_active`: Partial B-tree index on `(employee_id)` WHERE `effective_to IS NULL` for O(1) active salary lookups.
6. `idx_salary_effective_dates`: Composite index on `(employee_id, effective_from, effective_to)`.
7. `idx_audit_entity`: Composite index on `(entity_name, entity_id, created_at DESC)`.
8. `idx_audit_created_at`: Index on `created_at DESC` for paginated audit log browsing.

## 5. Constraint & Integrity Invariants
- `CHECK (amount > 0)`: Ensures non-negative and non-zero salary amounts.
- `CHECK (effective_to IS NULL OR effective_to >= effective_from)`: Prevents inverted date ranges.
- **Partial Unique Index**:
  ```sql
  CREATE UNIQUE INDEX uq_employee_active_salary 
  ON salary_records(employee_id) 
  WHERE effective_to IS NULL;
  ```
  Guarantees at the PostgreSQL engine level that an employee can never possess more than one concurrently active salary record.
