# Requirements Specification — ACME Employee Salary Management System

## 1. Problem Statement
ACME Corporation employs 10,000 personnel across multiple countries and departments. Human Resources (HR) currently tracks employee salaries and historical adjustments in fragmented, error-prone Excel spreadsheets. This manual workflow suffers from lack of concurrency control, no verifiable audit trail for sensitive financial modifications, vulnerability to unauthorized access or formula tampering, inability to prevent overlapping effective salary dates, and severe latency when leadership requests cross-country, currency-aware compensation analytics.

## 2. Primary Persona
**HR Manager**: A human resources professional responsible for maintaining accurate employee profiles, executing salary revisions with effective dates, inspecting compensation distributions across departments and geographic locations, and generating compliance-ready export reports.
*Secondary Personas*: **HR Admin** (system administrator managing reference data, user accounts, and audit inspection) and **HR Viewer** (auditor or executive reviewing read-only dashboards and employee compensation data).

## 3. Business Goal
Replace manual spreadsheet management with a centralized, secure, multi-user web application providing single-source-of-truth salary records, auditable effective-dated compensation tracking, server-side search and filtering across 10,000 employees, real-time aggregated salary analytics, and strict role-based access control.

## 4. In-Scope Features
1. **Authentication & RBAC**: JWT-based authentication (access + refresh tokens) with three granular roles (`HR_ADMIN`, `HR_MANAGER`, `HR_VIEWER`), token revocation, and rate-limited login.
2. **Employee Directory (10,000 Records)**: Seeded database with 10,000 synthetic employees across departments, countries, and job titles with server-side pagination, sorting, and multi-field search.
3. **Effective-Dated Salary Management**: Accurate salary assignment using `NUMERIC(15,2)`/`BigDecimal` amounts, ISO currency codes, pay frequency, and non-overlapping effective date ranges (`effective_from`, `effective_to`).
4. **Compensation History & Auditing**: Immutable historical salary transitions. Automatic closure of superseded records. Full audit logging capturing actor, timestamp, entity ID, and delta.
5. **Analytics & Dashboards**: Pre-aggregated database queries for headcount by country/department, salary distributions, min/max/average metrics normalized by pay frequency, and salary bands. Never aggregates incompatible currencies without currency grouping.
6. **Reports & Safe CSV Export**: Dynamic tabular reports with CSV export protected against CSV formula injection (`=`, `+`, `-`, `@`, `\t`, `\r`).
7. **Responsive UI**: Accessible Angular Material interface tested across desktop (1440px), laptop (1024px), tablet (768px), and mobile (360px).

## 5. Deliberately Out-of-Scope Features
1. **Payroll Disbursement & Banking Integration**: The system manages compensation planning and records; direct clearing house (ACH/SEPA/SWIFT) execution is handled by enterprise banking systems.
2. **Tax Filing & Statutory Deductions**: Multi-jurisdictional tax withholding calculations require external statutory payroll engines (e.g., ADP/Workday).
3. **Time, Attendance & Leave Tracking**: Handled by dedicated workforce management systems.
4. **Applicant Tracking (ATS) & Recruitment**: Post-hire compensation management only.
5. **Dynamic Exchange-Rate Conversions**: Cross-currency aggregation requires official accounting FX rates; analytics enforce single-currency views to prevent invalid financial equivalence.
6. **Enterprise SSO / SAML / OIDC**: Out of scope for standalone initial deployment; standard OAuth2/JWT architecture enables future federation.
7. **Distributed Microservices / Event Mesh**: A clean modular monolith provides superior transactional guarantees (ACID), zero network serialization overhead for 10,000 rows, and simplified operations.
8. **AI-Driven Compensation Recommendations**: Compliance requirements demand deterministic, human-driven compensation management.

## 6. Functional Requirements
- **FR-01**: Secure user login returning short-lived JWT access token (15m) and rotating refresh token (7d) stored securely.
- **FR-02**: Provide paginated employee search by code, name, and email with filters for department, country, job title, and employment status.
- **FR-03**: Create and edit employee records with server-side validation and uniqueness checks on `employee_code` and `email`.
- **FR-04**: Soft deactivation of employees (`status = INACTIVE` or `TERMINATED`) preventing orphan records in salary history or audit logs.
- **FR-05**: View active compensation and complete chronological salary history for any employee.
- **FR-06**: Add new salary revision within an ACID transaction: closes currently active salary record by setting `effective_to` to `new_effective_from - 1 day` and creates the new active record.
- **FR-07**: Reject overlapping salary effective dates and reject negative or zero salary amounts.
- **FR-08**: Provide organization-wide dashboard displaying active employee count, salary distributions by department/country, and min/max/avg compensation per currency.
- **FR-09**: HR reports filterable by country, department, currency, salary band, and modification date range.
- **FR-10**: Export filtered report data to CSV with sanitized fields escaping spreadsheet formula triggers.
- **FR-11**: Append-only audit log recording all employee and salary modifications (actor, IP, timestamp, action, old value, new value).

## 7. Non-Functional Requirements
- **NFR-01 Performance**: Paginated search across 10,000 employees responds in < 150ms at p95 using B-tree indexes. Dashboard aggregations complete in < 250ms via SQL `GROUP BY` execution.
- **NFR-02 Security**: Passwords hashed with BCrypt (strength 12). All endpoints protected by Spring Security RBAC. JWT signatures validated with HMAC-SHA256. Centralized CORS policy restricting origins to trusted frontend.
- **NFR-03 Reliability & Data Integrity**: Foreign keys on all relational references. Relational schema in 3NF. Optimistic locking (`@Version`) on salary records preventing lost updates.
- **NFR-04 Maintainability & Clean Architecture**: Modular layered monolith (Controller -> Service -> Repository -> Entity) with DTO abstraction, centralized `@ControllerAdvice`, and Bean Validation.
- **NFR-05 Auditability**: Strict append-only audit trail; no delete/update endpoints exposed for audit records.
- **NFR-06 Observability**: Spring Boot Actuator health (`/actuator/health`) and metrics endpoints; structured logging with SLF4J/Logback masking sensitive credentials.

## 8. Assumptions & Constraints
- **Assumptions**: HR Managers operate in modern evergreen browsers (Chrome, Edge, Firefox, Safari). Currency comparisons are valid only within the same currency code. Salary amounts are stored on an annualized or specified pay frequency basis (`ANNUAL`, `MONTHLY`, `BIWEEKLY`).
- **Constraints**: Mandatory Java 17, Spring Boot 3.x, Angular Material, PostgreSQL 16, Flyway migrations, Docker Compose deployment, and strict offline reproducibility without external cloud dependencies.

## 9. Acceptance Criteria
1. System initializes schema purely via versioned Flyway migrations from an empty database.
2. Deterministic data seeder generates exactly 10,000 valid, diverse employee records and test user accounts for `HR_ADMIN`, `HR_MANAGER`, and `HR_VIEWER`.
3. HR Manager can log in, search 10,000 employees seamlessly, view profiles, and update compensation with automatic superseded-record handling.
4. Overlapping salary date requests produce HTTP 400/409 with explanatory business error messages.
5. Dashboard renders real PostgreSQL aggregates without browser-side full-dataset loading.
6. CSV export succeeds and sanitizes formula-leading cells (`=`, `@`, `+`, `-`).
7. Full suite of backend unit/integration tests and frontend tests pass deterministically.

## 10. Success Measures
- **0 Data Discrepancies**: Zero duplicate active salary records for any employee at any point in time.
- **Sub-Second Analytics**: Dashboard aggregates on 10,000 records render in < 300ms.
- **100% Endpoint Protection**: Zero unauthenticated or unauthorized access to sensitive financial endpoints.
- **Zero Spreadsheet Dependencies**: Complete transition of ACME compensation workflows to the web platform.
