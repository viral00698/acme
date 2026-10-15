# Low-Level Design (LLD): Module C — Effective-Dated Salary Management

## 1. Responsibilities
- Managing employee compensation records using `BigDecimal` amounts.
- Enforcing effective date lifecycle rules and preventing overlapping salary periods.
- Atomically superseding active salary records upon compensation adjustment.
- Maintaining complete historical timeline of salary adjustments.
- Recording optimistic locking versions (`@Version`) to detect concurrent modifications.
- Firing audit log events for any compensation change.

## 2. Main Classes & Interfaces
- `com.acme.salary.salary.controller.SalaryController`: REST endpoints.
- `com.acme.salary.salary.service.SalaryService`: Transactional business orchestration.
- `com.acme.salary.salary.repository.SalaryRecordRepository`: JPA repository with custom queries for active/historical records.
- `com.acme.salary.salary.entity.SalaryRecord`: Entity mapped to `salary_records`.
- `com.acme.salary.salary.mapper.SalaryMapper`: Entity to DTO mapping.

## 3. Data Transfer Objects (DTOs)
- `SalaryRecordDto`: `{ id, employeeId, amount, currencyCode, currencySymbol, payFrequency, effectiveFrom, effectiveTo, createdAt, createdByUsername }`
- `CreateSalaryRequest`: `{ amount: BigDecimal (>0), currencyCode: string (3 chars), payFrequency: string (ANNUAL|MONTHLY|BIWEEKLY), effectiveFrom: LocalDate }`
- `SalaryHistoryResponse`: `{ employeeId: number, currentSalary: SalaryRecordDto, history: SalaryRecordDto[] }`

## 4. API Contract

### `GET /api/v1/employees/{id}/salaries`
- **Response**: `200 OK` -> `SalaryHistoryResponse`.
- **Authorization**: `HR_ADMIN`, `HR_MANAGER`, `HR_VIEWER`.

### `POST /api/v1/employees/{id}/salaries`
- **Request**: `CreateSalaryRequest`.
- **Response**: `201 Created` -> `SalaryRecordDto`.
- **Errors**: `400 Bad Request` (invalid amount or date), `404 Not Found` (employee missing), `409 Conflict` (overlapping date or optimistic lock failure).
- **Authorization**: `HR_ADMIN`, `HR_MANAGER`.

### `GET /api/v1/employees/{id}/salaries/{salaryId}`
- **Response**: `200 OK` -> `SalaryRecordDto`.
- **Authorization**: `HR_ADMIN`, `HR_MANAGER`, `HR_VIEWER`.

## 5. Transition Rules & Invariants
1. **Rule 1 (First Salary)**: If an employee has no existing salary records, a new record is created with `effective_from = request.effectiveFrom` and `effective_to = NULL`.
2. **Rule 2 (Superseding Active Salary)**:
   - Current active record has `effective_to IS NULL`.
   - `request.effectiveFrom` must be strictly greater than current active record's `effective_from`.
   - Active record is closed: `effective_to = request.effectiveFrom.minusDays(1)`.
   - New record is inserted with `effective_from = request.effectiveFrom` and `effective_to = NULL`.
3. **Rule 3 (Rejection of Past Overlaps)**: If `request.effectiveFrom <= current.effective_from`, the request is rejected with `SalaryOverlapException` (HTTP 409).
4. **Rule 4 (Transactional Isolation)**: The entire operation executes in `@Transactional(isolation = Isolation.READ_COMMITTED)`.
