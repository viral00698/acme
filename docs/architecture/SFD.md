# System Flow Diagram (SFD) — ACME Employee Salary Management System

## 1. System Interaction Flow Overview

The diagram below details the end-to-end request lifecycle from the HR Manager's browser to the database persistence layer, illustrating authentication, authorization, transactional domain logic, audit trail generation, and error mitigation.

```mermaid
sequenceDiagram
    autonumber
    actor HR as HR Manager / Admin
    participant UI as Angular SPA (Material UI)
    participant Interceptor as Auth HTTP Interceptor
    participant Security as Spring Security Filter Chain
    participant Controller as REST Controller (/api/v1/*)
    participant Service as Domain Service Layer (@Transactional)
    participant Repo as JPA Repository
    participant DB as PostgreSQL 16
    participant Audit as Audit Service

    Note over HR,UI: 1. User Interaction & Client Validation
    HR->>UI: Submit Form (e.g. Salary Revision)
    UI->>UI: Angular Reactive Form Validation (Client-Side)

    Note over UI,Security: 2. Request Interception & Bearer Token Injection
    UI->>Interceptor: Intercept Outgoing HTTP Request
    Interceptor->>Interceptor: Attach "Authorization: Bearer <JWT>"
    Interceptor->>Security: Dispatch HTTP POST /api/v1/employees/42/salaries

    Note over Security,Controller: 3. Security, Token Validation & RBAC
    Security->>Security: JwtAuthenticationFilter parses signature & claims
    alt Token Invalid or Expired
        Security-->>Interceptor: HTTP 401 Unauthorized
        Interceptor->>Interceptor: Execute POST /api/v1/auth/refresh
        alt Refresh Succeeded
            Interceptor->>Security: Re-send original request with new token
        else Refresh Failed
            Interceptor-->>UI: Redirect to /login
        end
    else Insufficient Role (e.g. HR_VIEWER attempting POST)
        Security-->>UI: HTTP 403 Forbidden (Access Denied)
    else Token Valid & Role Authorized
        Security->>Controller: Forward to Controller
    end

    Note over Controller,Service: 4. DTO Validation & Domain Service Execution
    Controller->>Controller: @Valid DTO Validation
    alt Validation Failed (e.g. negative salary)
        Controller-->>UI: HTTP 400 Bad Request (Field errors JSON)
    else Validation Succeeded
        Controller->>Service: updateSalary(employeeId, salaryRevisionDto)
    end

    Note over Service,DB: 5. Transaction Boundary & Business Invariants
    rect rgb(240, 248, 255)
        Service->>Repo: findActiveSalaryByEmployeeId(42)
        Repo->>DB: SELECT * FROM salary_records WHERE employee_id=42 AND effective_to IS NULL
        DB-->>Repo: Current Active Salary Record
        Repo-->>Service: ActiveSalaryRecord

        Service->>Service: Validate Date Range (new_effective_from > current_effective_from)
        alt Overlapping or Invalid Date
            Service-->>Controller: throw SalaryOverlapException
            Controller-->>UI: HTTP 409 Conflict ("Effective date overlaps existing record")
        else Valid Date Range
            Service->>Repo: save(closeExistingRecord(effective_to = new_from - 1))
            Repo->>DB: UPDATE salary_records SET effective_to='2026-10-31', version=version+1
            
            Service->>Repo: save(createNewSalaryRecord(effective_from=new_from, effective_to=NULL))
            Repo->>DB: INSERT INTO salary_records VALUES (...)
            
            Service->>Audit: recordSalaryChange(actor, employeeId, oldAmount, newAmount)
            Audit->>DB: INSERT INTO audit_logs VALUES (...)
        end
    end

    Note over Service,HR: 6. Response Marshalling & Client Presentation
    DB-->>Service: Commit Transaction
    Service-->>Controller: Return Updated SalaryResponseDto
    Controller-->>UI: HTTP 201 Created (JSON Response)
    UI->>UI: Update Angular Signals / RxJS State
    UI-->>HR: Display Success Toast & Render Updated Timeline
```

## 2. Key Process Flows

### 2.1 Authentication & Token Refresh Flow
1. **Login**: User submits username and password to `/api/v1/auth/login`. Authentication provider validates hashed password via BCrypt. If successful, generates an access token (15m expiry) and persists a hashed refresh token (7d expiry) in `refresh_tokens`.
2. **Silent Refresh**: When the access token expires, Angular's `AuthInterceptor` captures the `401 Unauthorized` response. It puts pending requests into a queue, calls `/api/v1/auth/refresh` with the refresh token, receives a new token pair, rotates the database refresh token, and replays all queued requests seamlessly.
3. **Logout**: Calling `/api/v1/auth/logout` explicitly deletes/revokes the refresh token record from PostgreSQL, preventing any subsequent silent re-authentication.

### 2.2 Salary Revision Workflow
- Enforces strict single-active-record invariant per employee.
- Checks that the new effective date is strictly after the prior record's start date.
- Supersedes the prior record by updating `effective_to` to `new_effective_from - 1 day`.
- Creates a new record with `effective_from` set and `effective_to = NULL`.
- Both operations and the audit record are executed within a single `@Transactional` method, guaranteeing atomicity.

### 2.3 Analytics & Reporting Flow
- Requests to `/api/v1/analytics/*` bypass individual entity retrieval.
- Repositories execute optimized SQL aggregation queries (`COUNT`, `AVG`, `MIN`, `MAX`, `GROUP BY department_id, currency`).
- Aggregations strictly partition by currency, preventing misleading mathematical blending across differing currencies.
- CSV exports stream results with formula-sanitizing transformation on all text fields.
