# Comprehensive Test Cases Matrix

## 1. Authentication & Security (Module A)

| ID | Title | Test Description | Expected Result | Automated Test / Location |
| :--- | :--- | :--- | :--- | :--- |
| **TC-AUTH-01** | Valid User Login | Submit valid credentials for `admin`, `manager`, `viewer`. | HTTP 200 OK; returns access JWT, refresh token, user details, role set. | `AuthenticationIntegrationTest.java` |
| **TC-AUTH-02** | Invalid Password | Submit registered username with wrong password. | HTTP 401 Unauthorized; generic error message to prevent enumeration. | `AuthenticationIntegrationTest.java` |
| **TC-AUTH-03** | Non-existent User | Submit unregistered username. | HTTP 401 Unauthorized; generic error message to prevent enumeration. | `AuthenticationIntegrationTest.java` |
| **TC-AUTH-04** | Access Token Rotation | Call `/api/v1/auth/refresh` with valid unrevoked refresh token. | HTTP 200 OK; returns brand new access token and rotated refresh token; old token revoked. | `AuthenticationIntegrationTest.java` |
| **TC-AUTH-05** | Revoked Refresh Token | Attempt to use an already rotated or revoked refresh token. | HTTP 401 Unauthorized; token rejection; security alert logged. | `AuthenticationIntegrationTest.java` |
| **TC-AUTH-06** | User Logout | Call `/api/v1/auth/logout` with active refresh token. | HTTP 204 No Content; refresh token deleted or marked revoked in DB. | `AuthenticationIntegrationTest.java` |
| **TC-AUTH-07** | Rate Limiting Lockout | Attempt 5 consecutive failed logins for the same username within 15 minutes. | 6th attempt returns HTTP 429 Too Many Requests with retry-after period. | `RateLimitingIntegrationTest.java` |
| **TC-AUTH-08** | Current User Profile | Call `/api/v1/auth/me` with valid Bearer token. | HTTP 200 OK; returns current authenticated user's profile and roles. | `AuthenticationIntegrationTest.java` |

---

## 2. Role-Based Access Control (RBAC)

| ID | Title | Role Under Test | Endpoint & Action | Expected Result | Automated Test / Location |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-RBAC-01** | Admin Full Privileges | `ROLE_HR_ADMIN` | POST `/api/v1/employees`<br>POST `/salaries`<br>GET `/audit-logs`<br>GET `/reports/salaries/export` | HTTP 200/201 across all operations. Full authority verified. | `RbacAuthorizationIntegrationTest.java` |
| **TC-RBAC-02** | Manager Mutation Rights | `ROLE_HR_MANAGER` | POST `/api/v1/employees`<br>POST `/salaries`<br>GET `/reports/salaries/export` | HTTP 200/201 for employee and salary creation and report exports. | `RbacAuthorizationIntegrationTest.java` |
| **TC-RBAC-03** | Manager Audit Restriction | `ROLE_HR_MANAGER` | GET `/api/v1/audit-logs` | HTTP 403 Forbidden. Audit trail restricted strictly to administrators. | `RbacAuthorizationIntegrationTest.java` |
| **TC-RBAC-04** | Viewer Read Authorization | `ROLE_HR_VIEWER` | GET `/api/v1/employees`<br>GET `/dashboard/overview`<br>GET `/reports/salaries` | HTTP 200 OK. Browsing and read-only inspection allowed. | `RbacAuthorizationIntegrationTest.java` |
| **TC-RBAC-05** | Viewer Mutation Restriction | `ROLE_HR_VIEWER` | POST `/api/v1/employees`<br>PUT `/api/v1/employees/{id}`<br>POST `/salaries` | HTTP 403 Forbidden. All write and update operations rejected. | `RbacAuthorizationIntegrationTest.java` |
| **TC-RBAC-06** | Viewer Export Restriction | `ROLE_HR_VIEWER` | GET `/api/v1/reports/salaries/export` | HTTP 403 Forbidden. Data exfiltration prevented for read-only role. | `RbacAuthorizationIntegrationTest.java` |

---

## 3. Employee Management (Module B)

| ID | Title | Test Description | Expected Result | Automated Test / Location |
| :--- | :--- | :--- | :--- | :--- |
| **TC-EMP-01** | Paginated Directory | Query `/api/v1/employees?page=0&size=20`. | HTTP 200 OK; returns exact 20 records, totalElements=10,000, totalPages=500. | `EmployeeIntegrationTest.java` |
| **TC-EMP-02** | Server-side Search | Search with `query=Sarah` or `query=EMP00001`. | Filtered result list matching code, first name, last name, or email. | `EmployeeIntegrationTest.java` |
| **TC-EMP-03** | Multi-attribute Filter | Filter with `departmentId=1&countryId=1&status=ACTIVE`. | SQL query applies composite index; returns exact matching subset. | `EmployeeIntegrationTest.java` |
| **TC-EMP-04** | Create Employee | POST `/api/v1/employees` with valid payload and initial salary. | HTTP 201 Created; employee record and initial salary record created atomically. | `EmployeeIntegrationTest.java` |
| **TC-EMP-05** | Duplicate Email Rejection | POST `/api/v1/employees` with already existing email. | HTTP 409 Conflict with descriptive message: `Work email already in use`. | `EmployeeIntegrationTest.java` |
| **TC-EMP-06** | Duplicate Code Rejection | POST `/api/v1/employees` with existing employee code. | HTTP 409 Conflict with message: `Employee code already exists`. | `EmployeeIntegrationTest.java` |
| **TC-EMP-07** | Bean Validation Failures | POST `/api/v1/employees` with empty name or invalid email syntax. | HTTP 400 Bad Request; field-level validation errors array returned. | `EmployeeIntegrationTest.java` |
| **TC-EMP-08** | Status Transition | PATCH `/api/v1/employees/{id}/status` to `TERMINATED`. | HTTP 200 OK; employee status updated; active salary closed. | `EmployeeIntegrationTest.java` |

---

## 4. Salary Management & Effective Dating (Module C)

| ID | Title | Test Description | Expected Result | Automated Test / Location |
| :--- | :--- | :--- | :--- | :--- |
| **TC-SAL-01** | Retrieve Current Salary | GET `/api/v1/employees/{id}/salaries/current`. | Returns single active salary record where `effective_end_date IS NULL`. | `SalaryIntegrationTest.java` |
| **TC-SAL-02** | Retrieve Salary History | GET `/api/v1/employees/{id}/salaries`. | Returns complete chronological salary revision list ordered by effective date DESC. | `SalaryIntegrationTest.java` |
| **TC-SAL-03** | Valid Salary Revision | POST `/api/v1/employees/{id}/salaries` with effective date after current. | Single transaction: previous salary closed (`effective_end_date = new_start - 1`), new salary created with NULL end date. | `SalaryIntegrationTest.java` |
| **TC-SAL-04** | Overlap: Same-day Collision | POST `/api/v1/employees/{id}/salaries` with effective start date equal to current record. | HTTP 409 Conflict; transaction rolled back; previous record remains intact. | `SalaryServiceTest.java` |
| **TC-SAL-05** | Overlap: Closed Range Collision | POST `/api/v1/employees/{id}/salaries` with effective start date falling inside a previously closed historical period. | HTTP 409 Conflict; message states collision with closed range. | `SalaryServiceTest.java` |
| **TC-SAL-06** | Overlap: Prior to Current | POST `/api/v1/employees/{id}/salaries` with start date prior to currently active salary record. | HTTP 409 Conflict; message explains revision date cannot precede active effective start. | `SalaryServiceTest.java` |
| **TC-SAL-07** | Negative Salary Rejection | POST `/api/v1/employees/{id}/salaries` with `amount: -500.00`. | HTTP 400 Bad Request; `@DecimalMin` validation constraint violated. | `SalaryIntegrationTest.java` |
| **TC-SAL-08** | Optimistic Locking | Concurrent updates with mismatched version integer. | HTTP 409 Conflict / ObjectOptimisticLockingFailureException handled gracefully. | `SalaryServiceTest.java` |

---

## 5. Analytics, Reporting & Safe CSV Export (Module D & E)

| ID | Title | Test Description | Expected Result | Automated Test / Location |
| :--- | :--- | :--- | :--- | :--- |
| **TC-REP-01** | Dashboard Overview | GET `/api/v1/analytics/overview`. | Returns total headcount, active headcount, and currency-isolated metrics. | `AnalyticsIntegrationTest.java` |
| **TC-REP-02** | Single-Currency Isolation | GET `/api/v1/analytics/currencies`. | Returns distinct cards per currency (`USD`, `EUR`, `GBP`, etc.) without cross-currency addition. | `AnalyticsIntegrationTest.java` |
| **TC-REP-03** | Salary Band Histograms | GET `/api/v1/analytics/distribution?currency=USD`. | Returns 5 standard salary bands with employee counts within USD range. | `AnalyticsIntegrationTest.java` |
| **TC-REP-04** | Multi-Criteria Reports | GET `/api/v1/reports/salaries?currency=USD&departmentId=1`. | Filtered paginated results matching criteria with execution under 50ms. | `ReportIntegrationTest.java` |
| **TC-REP-05** | Formula Injection Escaping | Trigger export on employee with name/note starting with `=CMD\|' /C calc'!A0`. | Exported cell is prepended with `'` (`'=CMD\|...`), disarming Excel execution. | `ReportIntegrationTest.java` |
| **TC-REP-06** | Streaming CSV Headers | GET `/api/v1/reports/salaries/export?currency=USD`. | `Content-Type: text/csv`, `Content-Disposition: attachment; filename="acme-salary-report-*.csv"`. | `ReportIntegrationTest.java` |

---

## 6. Audit Logging (Module F)

| ID | Title | Test Description | Expected Result | Automated Test / Location |
| :--- | :--- | :--- | :--- | :--- |
| **TC-AUD-01** | Salary Revision Audit | Execute salary revision for employee `EMP00001`. | New audit row recorded with `entity_type='SalaryRecord'`, action `UPDATE`, actor username, IP, before/after JSON diff. | `SalaryIntegrationTest.java` |
| **TC-AUD-02** | Employee Creation Audit | Execute employee creation. | New audit row recorded with `entity_type='Employee'`, action `CREATE`. | `EmployeeIntegrationTest.java` |
| **TC-AUD-03** | Audit Trail Filter | GET `/api/v1/audit-logs?actor=manager&entityType=SalaryRecord`. | Returns paginated audit entries matching actor and entity type. | `AuditIntegrationTest.java` |
| **TC-AUD-04** | Non-Admin Access Rejection | GET `/api/v1/audit-logs` using `manager` or `viewer` token. | HTTP 403 Forbidden. Strictly restricted to `ROLE_HR_ADMIN`. | `RbacAuthorizationIntegrationTest.java` |
