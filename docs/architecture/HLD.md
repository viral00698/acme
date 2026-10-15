# High-Level Design (HLD) — ACME Employee Salary Management System

## 1. Executive Summary & System Context
The **ACME Employee Salary Management System** is an enterprise-grade web application built to eliminate spreadsheet-based compensation tracking for 10,000 employees globally. It provides an authoritative, auditable repository for personnel salary profiles, historical revisions, real-time analytics, and CSV exports while enforcing role-based access control (RBAC).

```
+-------------------------------------------------------------------------+
|                               ACME Enterprise                           |
|                                                                         |
|  +--------------------+        HTTPS/TLS        +--------------------+  |
|  |     HR Manager     | <=====================> |    Angular 19/20   |  |
|  |     HR Admin       |                         |    Single Page App |  |
|  |     HR Viewer      |                         +--------------------+  |
|  +--------------------+                                   |             |
|                                                     REST / JSON (JWT)   |
|                                                           v             |
|                                                 +--------------------+  |
|                                                 | Spring Boot 3.3.4  |  |
|                                                 | Modular Monolith   |  |
|                                                 +--------------------+  |
|                                                           |             |
|                                                    JDBC / Hibernate     |
|                                                           v             |
|                                                 +--------------------+  |
|                                                 |   PostgreSQL 16    |  |
|                                                 |   Relational DB    |  |
|                                                 +--------------------+  |
+-------------------------------------------------------------------------+
```

## 2. Major Components & Responsibilities

### 2.1 Frontend: Single-Page Application (Angular 19/20)
- **Role**: Presentation and client-side orchestration.
- **Key Modules**:
  - `CoreModule`: Authentication state, JWT token storage, HTTP interceptor (token attachment and silent 401 refresh), route guards (`AuthGuard`, `RoleGuard`).
  - `FeaturesModule`:
    - `Dashboard`: Compensation overview, headcount metrics, salary bands, and distribution visualizations.
    - `Employees`: Paginated directory table, multi-field search, create/edit modals, profile details.
    - `Salaries`: Historical timeline, active compensation view, compensation revision modal with effective-date validation.
    - `Reports`: Multi-parameter filters, summary aggregations, formula-safe CSV export trigger.
    - `Audit`: Read-only system audit log viewer for HR Admins.
  - `SharedModule`: Angular Material components, confirmation dialogs, snackbar service, currency/date formatting pipes.

### 2.2 Backend: Modular Monolith (Spring Boot 3.3.4)
- **Role**: Business logic enforcement, transactional data access, security boundaries, and API contracts.
- **Layers**:
  - **Security Filter Chain**: Validates incoming Bearer JWT tokens, handles stateless sessions, enforces rate-limiting headers, and verifies RBAC roles.
  - **REST Controllers**: Thin HTTP adapters validating DTO schemas (`@Valid`) and returning standardized HTTP responses (`200 OK`, `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict`).
  - **Domain Services**: Encapsulate ACID transaction boundaries (`@Transactional`), business rules (e.g. effective date transitions, pay frequency normalization), and auditing.
  - **JPA Repositories**: Data access with typed Criteria/Specifications for dynamic search and native aggregation queries for analytics.
  - **Flyway Migration Engine**: Versioned SQL scripts initializing reference tables, domain entities, foreign keys, and indexes.

### 2.3 Database: PostgreSQL 16
- **Role**: Relational persistence, transactional ACID consistency, and indexing.
- **Key Characteristics**:
  - 3NF normalized schema for core entities.
  - Relational integrity through cascading rules and foreign key constraints.
  - B-tree indexing on frequently filtered fields (`employee_code`, `email`, `department_id`, `country_id`, `status`, `effective_from`, `effective_to`).
  - Optimistic locking (`version` column) preventing concurrent update anomalies.

## 3. Technology Stack Specification

| Tier | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java | 17 (LTS) | Modern syntax, records, pattern matching, JVM performance |
| **Framework** | Spring Boot | 3.3.4 | Pinned production release for REST, JPA, Security |
| **Security** | Spring Security + jjwt | 0.12.6 | Stateless JWT auth, BCrypt password hashing |
| **Database** | PostgreSQL | 16 | Relational store with ACID guarantees |
| **Migrations**| Flyway | 10.x | Deterministic, versioned schema management |
| **Documentation**| Springdoc OpenAPI | 2.6.0 | Swagger UI and OpenAPI 3.0 specification |
| **Frontend** | Angular | 19/20 | Component-based reactive SPA |
| **UI Library** | Angular Material | Compatible | Accessible, responsive design components |
| **State/Async**| RxJS | 7.8.x | Reactive event streams and HTTP handling |
| **Build Tool** | Apache Maven | 3.9.x | Backend dependency management and build lifecycle |
| **Deployment** | Docker & Compose | Multi-stage | Non-root containerization and orchestration |

## 4. API Communication & Authentication Flow
- All communications leverage standard HTTPS/TLS with JSON payload encoding.
- Base API path: `/api/v1`.
- **Authentication Lifecycle**:
  1. Client sends `POST /api/v1/auth/login` with username and password.
  2. Spring Security authenticates via `DaoAuthenticationProvider` using BCrypt (cost factor 12).
  3. System generates:
     - Short-lived JWT Access Token (15 minutes lifespan) containing user ID, username, and role claim.
     - Long-lived cryptographically random Refresh Token (7 days) stored as a SHA-256 hash in PostgreSQL.
  4. Angular stores access token in memory/session storage; Angular HTTP Interceptor attaches `Authorization: Bearer <token>` to all protected outgoing requests.
  5. Upon access token expiration (HTTP 401), the interceptor executes a single synchronized `POST /api/v1/auth/refresh` using the refresh token, updates the token pair, and retries the failed request.

## 5. Database Interaction & Transactional Boundaries
- **ACID Guarantees**: Critical operations (e.g. salary revisions, employee creation) are wrapped in `@Transactional(isolation = Isolation.READ_COMMITTED)`.
- **Salary History Continuity**:
  ```
  When updating Employee #1042's salary effective 2026-11-01:
  1. Acquire lock on employee record.
  2. Query active salary record (where effective_to IS NULL).
  3. Validate: new effective_from > current effective_from.
  4. Update active record: SET effective_to = '2026-10-31'.
  5. Insert new record: effective_from = '2026-11-01', effective_to = NULL.
  6. Insert audit log record: entity = SALARY_RECORD, action = REVISION.
  7. Commit transaction.
  ```
- **Read-Only Optimization**: Analytics and reporting queries utilize `@Transactional(readOnly = true)` to avoid Hibernate dirty-checking overhead.

## 6. Deployment Topology & Container Architecture
```
+-------------------------------------------------------------+
|                     Docker Host / VM                        |
|                                                             |
|   +-----------------------------------------------------+   |
|   |                  docker-compose                     |   |
|   |                                                     |   |
|   |  +--------------------+     +--------------------+  |   |
|   |  |   acme-frontend    |     |    acme-backend    |  |   |
|   |  |   (Nginx / Static) | ===>|  (Spring Boot Jar) |  |   |
|   |  |   Port 80 / 4200   |     |    Port 8080       |  |   |
|   |  +--------------------+     +--------------------+  |   |
|   |                                       |             |   |
|   |                                       v             |   |
|   |                             +--------------------+  |   |
|   |                             |   acme-postgres    |  |   |
|   |                             |   (PostgreSQL 16)  |  |   |
|   |                             |    Port 5432       |  |   |
|   |                             +--------------------+  |   |
|   |                                       |             |   |
|   |                                       v             |   |
|   |                             [postgres_data volume]  |   |
|   +-----------------------------------------------------+   |
+-------------------------------------------------------------+
```

## 7. Failure Handling & Resilience
- **Centralized Error Handling**: `GlobalExceptionHandler` intercepting:
  - `MethodArgumentNotValidException` -> HTTP 400 with structured per-field validation error list.
  - `EntityNotFoundException` -> HTTP 404 with clean resource error description.
  - `SalaryOverlapException` -> HTTP 409 Conflict with overlapping date range details.
  - `OptimisticLockingFailureException` -> HTTP 409 Conflict prompting user to refresh concurrent edit.
  - `AccessDeniedException` -> HTTP 403 Forbidden with security event logging.
- **Stateless Monolith Resilience**: Stateless architecture allows zero-downtime rolling updates behind a load balancer.

## 8. Scaling Strategy (10,000 Employees to 100,000+)
- **Indexed Search**: B-tree composite indexes ensure index scans rather than sequential scans.
- **Database Aggregations**: Mathematical aggregations (`COUNT`, `AVG`, `MIN`, `MAX`) happen directly inside PostgreSQL rather than serializing datasets to application memory.
- **Streaming Output**: CSV exports stream results via `ResponseBodyEmitter` / `StreamingResponseBody` directly from database cursors, avoiding memory bloat.
- **Horizontal Scaling**: Backend instances can scale horizontally behind Nginx/ALB since session state resides entirely in the database (refresh tokens) and client tokens (JWT).
