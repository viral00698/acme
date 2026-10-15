# Master Implementation Plan — ACME Employee Salary Management System

## Project Overview
ACME Employee Salary Management System replaces manual spreadsheet tracking for 10,000 employees with a secure, centralized, and auditable web platform. 

## Architectural Baseline
- **Backend**: Java 17, Spring Boot 3.3.4, Maven, Spring Data JPA, Hibernate, Spring Security, PostgreSQL, Flyway, Springdoc OpenAPI 2.6.0.
- **Frontend**: Angular 19/20, TypeScript Strict, Angular Material, RxJS, Reactive Forms.
- **Database**: PostgreSQL 16 with 3NF relational schema, B-tree indexes, and composite constraints.
- **Containerization**: Multi-stage Dockerfiles for backend and frontend with Docker Compose orchestration.

## Phased Implementation Roadmap

### Phase 1: Specifications, Architecture & Design Artifacts
- [x] Requirements Specification (`docs/requirements/requirements.md`)
- [x] Master Implementation Plan (`docs/architecture/implementation-plan.md`)
- [ ] High-Level Design (`docs/architecture/HLD.md`)
- [ ] System Flow Diagram (`docs/architecture/SFD.md`)
- [ ] Database Schema & Relational Design (`docs/architecture/database-design.md`)
- [ ] Security Architecture & Threat Model (`docs/architecture/security-design.md`)
- [ ] Scalability & Performance Strategy (`docs/architecture/scalability-and-performance.md`)
- [ ] Architectural Tradeoffs Analysis (`docs/architecture/tradeoffs.md`)
- [ ] Low-Level Design documents for all 5 functional modules (`docs/lld/*.md`)
- [ ] Complete Mermaid Diagrams (`docs/diagrams/*.mmd`)

### Phase 2: Application Scaffolding & Database Schema (Flyway)
- [ ] Initialize Git repository and `.gitignore`.
- [ ] Generate Maven multi-module or standard project layout (`backend/pom.xml`).
- [ ] Configure `application.yml`, `application-dev.yml`, `application-test.yml`, and `application-prod.yml`.
- [ ] Author Flyway V1 migration: reference tables (`departments`, `countries`, `currencies`, `job_titles`, `roles`).
- [ ] Author Flyway V2 migration: identity tables (`user_accounts`, `user_roles`, `refresh_tokens`).
- [ ] Author Flyway V3 migration: domain tables (`employees`, `salary_records`, `audit_logs`) with foreign keys, indexes, and constraints.
- [ ] Initialize Angular application (`frontend/`).

### Phase 3: Backend Core & Security Implementation (Module A)
- [ ] Domain Entities & Repositories (`UserAccount`, `Role`, `RefreshToken`, `AuditLog`).
- [ ] Spring Security configuration with stateless session management and JWT authentication filter.
- [ ] JWT token provider (access token generation, verification, claims parsing).
- [ ] Refresh token service with cryptographically secure token rotation and revocation.
- [ ] Authentication Controller & DTOs (`/api/v1/auth/login`, `/refresh`, `/logout`, `/me`).
- [ ] Centralized Exception Handling (`GlobalExceptionHandler`, `ApiErrorResponse`).
- [ ] Login rate limiting / brute-force protection filter.

### Phase 4: Reference Data & Employee Management (Module B)
- [ ] Reference entities (`Department`, `Country`, `Currency`, `JobTitle`) and endpoints.
- [ ] Employee Entity, DTOs, Repository with `JpaSpecificationExecutor`.
- [ ] Dynamic filtering Specification (search by code/name/email, filter by department, country, title, status).
- [ ] Employee Service with validation, unique code/email enforcement, and soft-delete/deactivation.
- [ ] Employee Controller (`/api/v1/employees`).
- [ ] Audit service integration for employee lifecycle changes.

### Phase 5: Effective-Dated Salary Management (Module C)
- [ ] SalaryRecord Entity with `BigDecimal`, `LocalDate` effective dates, optimistic locking `@Version`.
- [ ] Overlapping date validation logic and transactional revision handling:
  - Setting previous record's `effective_to = new_effective_from - 1 day`.
  - Creating new salary record in a single `@Transactional` boundary.
- [ ] Salary Service, DTOs, and Controller (`/api/v1/employees/{id}/salaries`).
- [ ] Comprehensive validation rules (positive salary, valid currency, frequency matching).
- [ ] Audit trail logging for salary adjustments.

### Phase 6: Analytics, Dashboards & Reporting (Modules D & E)
- [ ] Repository native / JPQL aggregation queries for headcount and compensation statistics.
- [ ] Dashboard Service (`/api/v1/analytics/overview`, `/salary-distribution`, `/by-country`, `/by-department`).
- [ ] Normalization of salary frequencies (Annualized calculation = Monthly * 12, Biweekly * 26, etc.).
- [ ] Strict single-currency grouping (preventing cross-currency aggregation errors).
- [ ] Report Service & Controller (`/api/v1/reports/salaries`).
- [ ] Streaming CSV export service with CSV formula injection protection (`=`, `@`, `+`, `-`).

### Phase 7: Audit Logging & System Administration (Module F)
- [ ] Append-only audit log persistence and query service (`/api/v1/audit-logs`).
- [ ] Pagination, actor filtering, and entity filtering for audit logs.
- [ ] Role enforcement ensuring only `HR_ADMIN` can inspect audit logs.

### Phase 8: Deterministic 10,000 Employee Seed Data & Test Accounts
- [ ] High-performance batch data seeder generating 10,000 realistic synthetic employees.
- [ ] Deterministic distributions across 6 countries, 8 departments, 25 job titles, and multiple currencies.
- [ ] Multi-record salary history for selected employee subsets.
- [ ] Creation of seeded test accounts (`admin`, `manager`, `viewer`) with encrypted BCrypt passwords.
- [ ] Configurable CLI runner / Spring profile flag (`--seed=true` or dev runner).

### Phase 9: Angular Frontend Implementation
- [ ] Layout shell with responsive sidebar navigation, header, and user profile badge.
- [ ] Core authentication state management, JWT storage, and HTTP interceptor with auto-refresh.
- [ ] Route guards for authentication and role-based route access (`HR_ADMIN`, `HR_MANAGER`, `HR_VIEWER`).
- [ ] Screens:
  1. Login Screen with validation and error alerts.
  2. Analytics Dashboard with summary cards and distribution charts.
  3. Employee List with server-side pagination, search input, and multi-select filters.
  4. Employee Detail & Profile editing.
  5. Salary History Timeline & Compensation Revision Dialog.
  6. Filterable Reports Table & Safe CSV Export Button.
  7. Audit Log Viewer with filtering.
- [ ] Responsive design verification at 360px, 768px, 1024px, and 1440px.

### Phase 10: Testing, Hardening & Verification
- [ ] Backend Unit Tests (JUnit 5 + Mockito) for auth, salary transitions, validation, and analytics.
- [ ] Backend Integration Tests (MockMvc + DataJpaTest) verifying endpoints, transactions, and security.
- [ ] Frontend Unit Tests for components, services, and interceptor.
- [ ] Performance testing script on 10,000 employees verifying p95 latency targets.
- [ ] Security audit: OWASP Top 10 checklist, CORS validation, sanitization.

### Phase 11: Dockerization, Deployment & Documentation Delivery
- [ ] Multi-stage `backend/Dockerfile` and `frontend/Dockerfile`.
- [ ] `docker-compose.yml` linking Postgres 16, backend, and frontend.
- [ ] Complete documentation: `local-setup.md`, `docker-deployment.md`, `production-readiness.md`.
- [ ] AI prompt records (`docs/ai/prompts/*.md`) and comprehensive `AI_WORKLOG.md`.
- [ ] Final verification against Definition of Done checklist.
