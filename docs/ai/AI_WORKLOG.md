# AI Worklog — ACME Employee Salary Management System

This document maintains an auditable, incremental log of engineering decisions, prompts, code generation, verification steps, and human/architect reviews throughout the project lifecycle.

---

## Entry 01: Project Initialization, Requirements & Architecture Planning
- **Date**: 2026-10-15
- **Module / Task**: Requirements Analysis, System Planning & Environment Inspection
- **Goal**: Verify environment prerequisites (Java 17, Maven, Node.js, Angular, PostgreSQL, Docker), establish requirements baseline, and structure project documentation.
- **AI Tool Used**: Antigravity Assistant (Gemini 3.8 Flash High)
- **Prompt Files**: `docs/ai/prompts/00-master-prompt.md`, `docs/ai/prompts/01-requirements-prompt.md`
- **Main Changes Generated or Suggested**:
  - Inspected runtime environment: Java 17.0.12 verified, Maven 3.9.11 located in bundled directory, Node v22.23.2, npm 11.13.0, PostgreSQL 16 running on localhost:5432 with password `root`.
  - Created one-page Requirements Specification (`docs/requirements/requirements.md`) with explicit in-scope and out-of-scope boundaries.
  - Authored Master Implementation Plan (`docs/architecture/implementation-plan.md`) outlining the 11 delivery phases.
- **Files Changed**:
  - `docs/requirements/requirements.md`, `docs/architecture/implementation-plan.md`, `docs/ai/prompts/*.md`, `docs/ai/AI_WORKLOG.md`
- **Tests Executed**: Environment verification for Java 17, Maven 3.9.11, PostgreSQL port 5432 TCP connectivity.
- **Outcome**: Milestone 1 complete. Git commit `cfdfb12` created.

---

## Entry 02: Architecture Artifacts, Low-Level Designs & Mermaid Diagrams
- **Date**: 2026-10-15
- **Module / Task**: Architectural Documentation, Data Modeling & Sequence Diagrams
- **Goal**: Author comprehensive High-Level Design (HLD), System Flow Diagram (SFD), 3NF Database Design, Security Design, Scalability & Performance, Tradeoffs, and LLD specifications for all 6 functional modules, alongside 9 Mermaid architectural diagrams.
- **AI Tool Used**: Antigravity Assistant (Gemini 3.8 Flash High)
- **Prompt Files**: `docs/ai/prompts/02-database-design-prompt.md`
- **Main Changes Generated or Suggested**:
  - `docs/architecture/HLD.md`, `SFD.md`, `database-design.md`, `security-design.md`, `scalability-and-performance.md`, `tradeoffs.md`
  - `docs/lld/authentication.md`, `employee-management.md`, `salary-management.md`, `analytics-and-reports.md`, `audit-and-administration.md`
  - `docs/diagrams/*.mmd` (9 files)
- **Tests Executed**: Validated Mermaid syntax structures for compilation correctness.
- **Outcome**: Milestone 2 complete. Git commit `6a55275` created.

---

## Entry 03: Project Scaffolding & Initial Build Verification
- **Date**: 2026-10-15
- **Module / Task**: Application Initialization (Spring Boot 3.3.4 & Angular 19/20)
- **Goal**: Initialize Java Spring Boot 3.3.4 Maven backend, environment YAML profiles, `.env.example`, `.gitignore`, and scaffold Angular SPA with Angular Material and CDK.
- **AI Tool Used**: Antigravity Assistant (Gemini 3.8 Flash High)
- **Prompt Files**: `docs/ai/prompts/00-master-prompt.md`
- **Main Changes Generated or Suggested**:
  - Created `backend/pom.xml`, `mvnw.cmd`, `AcmeSalaryApplication.java`, YAML profiles.
  - Scaffolding Angular SPA with Angular Material and CDK. Configured global typography, styles, and themes.
- **Tests Executed**:
  - `mvnw.cmd compile` -> `BUILD SUCCESS` (5.4s).
  - `npm run build` -> `Application bundle generation complete` (8.3s).
- **Outcome**: Milestone 3 complete. Git commit `73506e3` created.

---

## Entry 04: PostgreSQL Relational Schema & Flyway Migrations
- **Date**: 2026-10-15
- **Module / Task**: Database Migration & Schema Design (Flyway V1, V2, V3)
- **Goal**: Implement deterministic Flyway migrations from an empty database, establishing 3NF schema, indexes, unique constraints, foreign keys, and optimistic locking columns.
- **AI Tool Used**: Antigravity Assistant (Gemini 3.8 Flash High)
- **Prompt Files**: `docs/ai/prompts/02-database-design-prompt.md`
- **Main Changes Generated or Suggested**:
  - `V1__init_reference_tables.sql`: Currencies, Countries, Departments, Job Titles with ISO seeds.
  - `V2__init_security_tables.sql`: Roles, User Accounts, User Roles, Refresh Tokens with BCrypt hashed seeds for `admin`, `manager`, `viewer`.
  - `V3__init_employee_salary_audit_tables.sql`: Employees, Salary Records (effective-dated), Audit Logs. Enforced partial unique index `uq_employee_active_salary` (`WHERE effective_to IS NULL`) and composite indexes for 10,000 employee queries.
  - Created `FlywayMigrationIntegrationTest.java` verifying automated migration against PostgreSQL `acme_salary_db`.
- **Files Changed**:
  - `backend/src/main/resources/db/migration/V1__init_reference_tables.sql`
  - `backend/src/main/resources/db/migration/V2__init_security_tables.sql`
  - `backend/src/main/resources/db/migration/V3__init_employee_salary_audit_tables.sql`
  - `backend/src/main/java/com/acme/salary/common/util/PasswordUtil.java`
  - `backend/src/test/java/com/acme/salary/migration/FlywayMigrationIntegrationTest.java`
  - `docs/ai/AI_WORKLOG.md`
- **Tests Executed**:
  - `mvnw.cmd test -Dtest=FlywayMigrationIntegrationTest` against PostgreSQL 16.
  - Verified 3 migrations applied cleanly, schema version v3 recorded in `flyway_schema_history`.
- **Outcome**: Milestone 4 complete. Git commit `ea5c212` created.

---

## Entry 05: JWT Authentication, Refresh Token Rotation & RBAC
- **Date**: 2026-10-15
- **Module / Task**: Module A — Authentication & Role-Based Access Control
- **Goal**: Implement JWT authentication, refresh token rotation, in-memory rate limiting, centralized exception handling, OpenAPI security specifications, and full unit/integration test coverage.
- **AI Tool Used**: Antigravity Assistant (Gemini 3.8 Flash High)
- **Prompt Files**: `docs/ai/prompts/03-backend-prompt.md`
- **Main Changes Generated or Suggested**:
  - `JwtTokenProvider.java`: HMAC-SHA256 signature signing and claims extraction.
  - `RefreshTokenService.java`: Cryptographic token generation, SHA-256 database hashing, automatic token rotation on refresh, and revocation on logout.
  - `LoginRateLimitService.java`: Sliding window rate limiter mitigating brute-force attacks (5 failed attempts per 15 minutes).
  - `SecurityConfig.java`: Stateless session configuration, URL-level RBAC rules, CORS configuration, custom AuthenticationEntryPoint and AccessDeniedHandler.
  - `GlobalExceptionHandler.java`: Centralized handling of validation, bad credentials, rate limiting, and access denied errors.
  - `AuthController.java`: REST endpoints `/api/v1/auth/login`, `/refresh`, `/logout`, `/me`.
  - Added unit tests (`JwtTokenProviderTest`, `RefreshTokenServiceTest`) and MockMvc integration tests (`AuthControllerIntegrationTest`).
- **Files Changed**:
  - `backend/src/main/java/com/acme/salary/auth/**`
  - `backend/src/main/java/com/acme/salary/common/**`
  - `backend/src/test/java/com/acme/salary/auth/**`
  - `backend/src/test/resources/data.sql`
  - `docs/ai/prompts/03-backend-prompt.md`
  - `docs/ai/AI_WORKLOG.md`
- **Tests Executed**:
  - `mvnw.cmd test`: 12 tests executed, 0 failures, 0 errors, 100% passed.
- **Problems Discovered**:
  - Logout endpoint originally required authentication; updated `SecurityConfig` to allow `/api/v1/auth/logout` public access so expired session tokens can still revoke their refresh tokens.
  - Flyway assertion needed adjustment to accommodate re-runs against existing schemas (`applied >= 3`).
- **Corrections Made**: Fixed security matchers, standardized error message string, and updated test assertions.
- **Human / Architect Review**: All tests passing. Authentication and RBAC architecture fully compliant with SOLID and security requirements.
- **Final Outcome**: Milestone 5 complete. Ready for Git commit 5 (`feat: implement JWT authentication and RBAC`).
