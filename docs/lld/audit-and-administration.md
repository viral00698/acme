# Low-Level Design (LLD): Module F — Audit & Administration

## 1. Responsibilities
- Record immutable, append-only audit entries for all critical mutations:
  - Employee creation, profile updates, status changes.
  - Salary additions and revisions.
  - User authentication and authorization failures.
- Provide paginated inspection of audit records exclusively to `HR_ADMIN`.
- Manage reference datasets (departments, countries, currencies, job titles).

## 2. Main Classes & Interfaces
- `com.acme.salary.audit.controller.AuditController`: REST endpoints restricted to `HR_ADMIN`.
- `com.acme.salary.audit.service.AuditService`: Append-only recording and paginated retrieval.
- `com.acme.salary.audit.repository.AuditLogRepository`: Spring Data JPA repository.
- `com.acme.salary.audit.entity.AuditLog`: JPA entity mapped to `audit_logs`.
- `com.acme.salary.organization.controller.OrganizationController`: Reference data APIs.
- `com.acme.salary.organization.service.OrganizationService`: Reference data lookup service.

## 3. Data Transfer Objects (DTOs)
- `AuditLogDto`: `{ id, actorUsername, entityName, entityId, action, detailsJson, clientIp, createdAt }`
- `DepartmentDto`: `{ id, code, name }`
- `CountryDto`: `{ id, code, name, defaultCurrencyCode }`
- `CurrencyDto`: `{ code, name, symbol }`
- `JobTitleDto`: `{ id, title, departmentId, departmentName }`

## 4. API Contract

### `GET /api/v1/audit-logs`
- **Query Params**: `entityName`, `entityId`, `actor`, `page` (default 0), `size` (default 20).
- **Response**: `200 OK` -> `PageResponse<AuditLogDto>`.
- **Authorization**: `ROLE_HR_ADMIN` only.

### Reference Data Endpoints:
- `GET /api/v1/organization/departments`
- `GET /api/v1/organization/countries`
- `GET /api/v1/organization/currencies`
- `GET /api/v1/organization/job-titles`
- **Authorization**: `HR_ADMIN`, `HR_MANAGER`, `HR_VIEWER`.

## 5. Security & Immutability Rules
- The `AuditLog` entity has NO `@Update` or `@Delete` operations mapped.
- Database trigger or application-level rules prevent modifications to existing audit records.
- Sensitive values (passwords, JWT tokens, payment card details) are strictly omitted from `detailsJson`.
