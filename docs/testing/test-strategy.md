# Test Strategy & Quality Assurance Framework

## 1. Overview
The ACME Employee Salary Management System utilizes a multi-layered testing pyramid ensuring functional correctness, data integrity, transactional consistency, security adherence, and formula injection defenses across all operational tiers.

```
       /\
      /  \     End-to-End & Smoke Tests
     /----\    (Cross-browser, Cypress/Playwright/Manual)
    /      \   Integration & Security Tests (MockMvc, Testcontainers)
   /--------\  (RBAC, Rate Limiting, JPA Transactions, Flyway Schema)
  /          \ Unit Tests (JUnit 5, Mockito, Vitest)
 /------------\ (Services, Sanitizers, Validators, Signal State)
```

## 2. Backend Testing Strategy

### 2.1 Unit Tests (JUnit 5 + Mockito)
- **Domain Services**:
  - `SalaryServiceTest`: Comprehensive coverage of effective-dated revisions, same-day collision rejection, historical range overlaps, and active salary closing.
  - `EmployeeServiceTest`: Employee creation, code/email uniqueness checks, and status modifications.
  - `AnalyticsServiceTest`: Aggregation and band computations.
  - `OrganizationServiceTest`: Reference lookups and department filtering.
  - `RefreshTokenServiceTest`: Cryptographic token generation, SHA-256 database hashing, and rotation validation.
- **Utility & Exception Handling**:
  - `CsvSanitizerTest`: Thorough edge-case testing of formula injection characters (`=`, `+`, `-`, `@`, `\t`, `\r`) with single-quote escaping.
  - `GlobalExceptionHandlerTest`: Verification of standard `ApiErrorResponse` serialization and HTTP status mappings.

### 2.2 Integration & Security Tests (Spring Boot Test + MockMvc)
- **Authentication & Rate Limiting**:
  - `AuthControllerIntegrationTest`: Login, token refresh, logout, and `/me` endpoints.
  - `RateLimitingIntegrationTest`: Brute-force protection verifying that exceeding 5 failed login attempts triggers HTTP 429 Too Many Requests with sliding window lockout.
- **Role-Based Access Control (RBAC)**:
  - `RbacAuthorizationIntegrationTest`: Strict permission assertions ensuring:
    - Unauthenticated requests receive 401 Unauthorized.
    - `HR_VIEWER` can read directory, reports, and analytics, but receives 403 Forbidden on employee mutation, salary revisions, CSV exports, and audit logs.
    - `HR_MANAGER` can modify profiles, revise salaries, and export CSVs, but receives 403 Forbidden on audit logs.
    - `HR_ADMIN` has unrestricted access across all endpoints including audit trails.
- **Database Migrations**:
  - `FlywayMigrationIntegrationTest`: Ensures clean schema initialization from empty PostgreSQL instance, verifying all tables, foreign keys, and indexes.

## 3. Frontend Testing Strategy (Vitest + Jasmine/Karma)
- **Component Tests**: Verifies creation, rendering, user interactions, and reactive form state.
- **Service & Guard Tests**: Verifies HTTP requests, token persistence in localStorage, and RBAC route navigation guards.

## 4. Key Business Invariants Tested
1. **Single-Active-Salary Invariant**: Partial unique index `uq_employee_active_salary (employee_id) WHERE effective_to IS NULL` guaranteed by transactional service closing previous record (`effective_to = newEffectiveFrom.minusDays(1)`).
2. **Currency Segregation**: Salaries in different currencies are never summed or averaged together.
3. **Formula Injection Immunity**: Exported CSV cells never trigger formula execution in Excel or Google Sheets.
