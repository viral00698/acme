# Test Execution Results & Metrics

## 1. Summary of Execution
- **Date**: 2026-10-15
- **Backend Test Framework**: JUnit 5, Mockito, Spring Boot Test, MockMvc
- **Total Backend Tests**: 62
- **Passed**: 62
- **Failed**: 0
- **Skipped**: 0
- **Duration**: 25.3 seconds
- **Pass Rate**: 100.0%

---

## 2. Detailed Test Breakdown by Module

| Test Class | Focus Area | Tests Run | Pass | Fail | Execution Time |
| :--- | :--- | :---: | :---: | :---: | :---: |
| `AnalyticsControllerIntegrationTest` | Dashboard & Breakdown Endpoints | 4 | 4 | 0 | 11.35s |
| `AnalyticsServiceTest` | Aggregations & Distribution Bands | 2 | 2 | 0 | 0.68s |
| `AuditControllerIntegrationTest` | Audit Trail Access & RBAC | 3 | 3 | 0 | 0.33s |
| `AuthControllerIntegrationTest` | Authentication, JWT, Refresh, Me | 5 | 5 | 0 | 1.85s |
| `JwtTokenProviderTest` | Token Generation, Expiry & Signature | 3 | 3 | 0 | 0.04s |
| `RateLimitingIntegrationTest` | Sliding Window 5-Attempt Lockout (429) | 1 | 1 | 0 | 1.55s |
| `RefreshTokenServiceTest` | Token Hash, Rotation & Revocation | 3 | 3 | 0 | 0.16s |
| `GlobalExceptionHandlerTest` | Standardized ApiErrorResponse Mappings | 5 | 5 | 0 | 0.01s |
| `EmployeeControllerIntegrationTest` | Paginated Query & Mutations | 4 | 4 | 0 | 0.23s |
| `EmployeeServiceTest` | Business Logic & Code/Email Uniqueness | 3 | 3 | 0 | 0.22s |
| `FlywayMigrationIntegrationTest` | Automated Schema Migrations (V1-V3) | 1 | 1 | 0 | 0.60s |
| `OrganizationServiceTest` | Department, Country, Currency Lookups | 4 | 4 | 0 | 0.10s |
| `ReportControllerIntegrationTest` | Filtered Query & CSV Export Endpoint | 3 | 3 | 0 | 0.28s |
| `CsvSanitizerTest` | Formula Injection Defense (`=`, `+`, `-`, `@`) | 7 | 7 | 0 | 0.06s |
| `SalaryControllerIntegrationTest` | History & Effective Revision Endpoints | 4 | 4 | 0 | 0.21s |
| `SalaryServiceTest` | Effective Overlap, Same-Day & Revisions | 6 | 6 | 0 | 0.04s |
| `RbacAuthorizationIntegrationTest` | Role Matrix Verification (Viewer, Mgr, Admin) | 4 | 4 | 0 | 0.14s |

---

## 3. Frontend Test Results
- **Test Runner**: Vitest (v5.0.4)
- **Total Test Files**: 7
- **Total Tests**: 22
- **Passed**: 22
- **Failed**: 0
- **Pass Rate**: 100.0%
- **Duration**: 5.87 seconds

| Test Spec File | Tested Component / Service | Tests Run | Pass | Fail | Execution Time |
| :--- | :--- | :---: | :---: | :---: | :---: |
| `app.spec.ts` | App Root & Router Outlet | 2 | 2 | 0 | 130ms |
| `auth.service.spec.ts` | JWT Storage, Roles & Lifecycle | 4 | 4 | 0 | 118ms |
| `auth.guard.spec.ts` | Route Protection & Redirect | 3 | 3 | 0 | 98ms |
| `role.guard.spec.ts` | Role-Based Authorization Guard | 3 | 3 | 0 | 101ms |
| `login.component.spec.ts` | Form Validation & Presets | 5 | 5 | 0 | 808ms |
| `dashboard.component.spec.ts`| KPI & Currency Band Loading | 2 | 2 | 0 | 601ms |
| `reports.component.spec.ts`  | Filter State & CSV Streaming | 3 | 3 | 0 | 969ms |

