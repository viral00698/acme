# Low-Level Design (LLD): Module B — Employee Management

## 1. Responsibilities
- Directory management for 10,000 employees.
- Server-side paginated querying with multi-field search and filters.
- Employee creation and profile updating with business rule validation.
- Uniqueness validation for `employee_code` and `email`.
- Employee status management (`ACTIVE`, `INACTIVE`, `TERMINATED`, `ON_LEAVE`).
- Prevention of hard physical deletion for records with financial or audit dependencies.

## 2. Main Classes & Interfaces
- `com.acme.salary.employee.controller.EmployeeController`: REST endpoints.
- `com.acme.salary.employee.service.EmployeeService`: Business logic, uniqueness checks, and deactivation handling.
- `com.acme.salary.employee.repository.EmployeeRepository`: Spring Data JPA repository extending `JpaSpecificationExecutor<Employee>`.
- `com.acme.salary.employee.repository.EmployeeSpecification`: Specification factory for dynamic Criteria queries (search query, departmentId, countryId, jobTitleId, status).
- `com.acme.salary.employee.entity.Employee`: JPA entity mapping to `employees` table.
- `com.acme.salary.employee.mapper.EmployeeMapper`: DTO-entity conversions.

## 3. Data Transfer Objects (DTOs)
- `EmployeeSummaryDto`: Lightweight record for list table (`id`, `employeeCode`, `firstName`, `lastName`, `email`, `departmentName`, `countryName`, `jobTitle`, `status`, `hireDate`).
- `EmployeeDetailDto`: Comprehensive profile with current salary summary.
- `CreateEmployeeRequest`: Input payload with `@NotBlank`, `@Email`, foreign key IDs, and hire date.
- `UpdateEmployeeRequest`: Payload for updating profile information.
- `UpdateStatusRequest`: Payload for changing employment status.

## 4. API Contract

### `GET /api/v1/employees`
- **Query Params**: `page` (default 0), `size` (default 20, max 100), `search` (optional string), `departmentId`, `countryId`, `jobTitleId`, `status`, `sortBy` (default `id`), `sortDir` (default `asc`).
- **Response**: `200 OK` -> `PageResponse<EmployeeSummaryDto>`.
- **Authorization**: `HR_ADMIN`, `HR_MANAGER`, `HR_VIEWER`.

### `GET /api/v1/employees/{id}`
- **Response**: `200 OK` -> `EmployeeDetailDto`.
- **Errors**: `404 Not Found`.

### `POST /api/v1/employees`
- **Request**: `CreateEmployeeRequest`.
- **Response**: `201 Created` -> `EmployeeDetailDto`.
- **Errors**: `400 Bad Request` (validation failed), `409 Conflict` (duplicate code or email).
- **Authorization**: `HR_ADMIN`, `HR_MANAGER`.

### `PUT /api/v1/employees/{id}`
- **Request**: `UpdateEmployeeRequest`.
- **Response**: `200 OK` -> `EmployeeDetailDto`.
- **Authorization**: `HR_ADMIN`, `HR_MANAGER`.

### `PATCH /api/v1/employees/{id}/status`
- **Request**: `UpdateStatusRequest`.
- **Response**: `200 OK` -> `EmployeeDetailDto`.
- **Authorization**: `HR_ADMIN`, `HR_MANAGER`.
