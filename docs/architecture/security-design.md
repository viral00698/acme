# Security Architecture & Threat Model — ACME Employee Salary Management System

## 1. Overview
The ACME Employee Salary Management System processes confidential personnel compensation figures. Security is built into the architecture using defense-in-depth principles across transport, authentication, authorization, data validation, auditability, and output encoding.

## 2. Authentication & Credential Architecture

### 2.1 Password Security
- Passwords are encrypted using **BCrypt** with a work factor (cost) of 12 via Spring Security's `PasswordEncoder`.
- Passwords are never logged, serialized into DTOs, or stored in plaintext.

### 2.2 JWT Access Tokens
- **Algorithm**: HMAC-SHA256 (`HS256`) signed with a minimum 256-bit cryptographically secure secret (`JWT_SECRET` injected via environment variable).
- **Lifespan**: 15 minutes (short-lived to minimize replay exposure).
- **Payload Claims**:
  - `sub`: User ID (e.g. `101`)
  - `username`: Username string (e.g. `manager`)
  - `roles`: Collection of assigned authorities (e.g. `["ROLE_HR_MANAGER"]`)
  - `iat`: Issued-at timestamp
  - `exp`: Expiration timestamp

### 2.3 Rotating Refresh Tokens
- **Generation**: Cryptographically secure 64-byte random string generated via `SecureRandom`.
- **Database Storage**: The database stores only the **SHA-256 hash** of the refresh token (`token_hash`), protecting active sessions even in the unlikely event of database exfiltration.
- **Rotation**: Every successful refresh invocation revokes the consumed token and issues a brand-new token pair.
- **Revocation**: Explicit logout immediately marks the token as revoked and removes it from valid sessions.

## 3. Role-Based Access Control (RBAC) Matrix

| Endpoint / Operation | HR_ADMIN | HR_MANAGER | HR_VIEWER | Unauthenticated |
| :--- | :---: | :---: | :---: | :---: |
| `POST /api/v1/auth/login` | Yes | Yes | Yes | Yes |
| `POST /api/v1/auth/refresh` | Yes | Yes | Yes | Yes |
| `POST /api/v1/auth/logout` | Yes | Yes | Yes | No |
| `GET /api/v1/auth/me` | Yes | Yes | Yes | No |
| `GET /api/v1/employees` | Yes | Yes | Yes | No |
| `GET /api/v1/employees/{id}` | Yes | Yes | Yes | No |
| `POST /api/v1/employees` | Yes | Yes | No (403) | No |
| `PUT /api/v1/employees/{id}` | Yes | Yes | No (403) | No |
| `PATCH /api/v1/employees/{id}/status` | Yes | Yes | No (403) | No |
| `GET /api/v1/employees/{id}/salaries` | Yes | Yes | Yes | No |
| `POST /api/v1/employees/{id}/salaries` | Yes | Yes | No (403) | No |
| `GET /api/v1/analytics/**` | Yes | Yes | Yes | No |
| `GET /api/v1/reports/**` | Yes | Yes | Yes | No |
| `GET /api/v1/reports/salaries/export` | Yes | Yes | No (403) | No |
| `GET /api/v1/audit-logs/**` | Yes | No (403) | No (403) | No |
| Reference Data (`/departments`, etc.) | Read/Write | Read-Only | Read-Only | No |

## 4. Threat Model & Mitigation Countermeasures

### 4.1 Injection Attacks
- **SQL Injection**: Prevented using Spring Data JPA, Hibernate criteria builders, and parameterized native queries. No dynamic string concatenation of SQL is permitted.
- **CSV Formula Injection**: Spreadsheets treat cells starting with `=`, `+`, `-`, `@`, `\t`, or `\r` as executable macros. The CSV export sanitizer prepends a single quotation mark (`'`) to any text field beginning with these characters.

### 4.2 Cross-Origin Resource Sharing (CORS)
- Hardcoded wildcard `*` CORS origins are prohibited.
- `CorsConfiguration` explicitly allows only authorized frontend origins (e.g., `http://localhost:4200`, `http://localhost:80`, `https://*.acme.corp`) with explicit HTTP verbs (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`) and credentials enabled (`allowCredentials = true`).

### 4.3 Brute-Force & Rate Limiting
- Login attempts are tracked using an in-memory sliding window cache per IP / username (`LoginRateLimitService`).
- More than 5 consecutive failed login attempts within 15 minutes trigger a temporary block (HTTP 429 Too Many Requests).

### 4.4 Information Leakage
- Passwords, authorization headers, and raw tokens are excluded from SLF4J log patterns.
- `GlobalExceptionHandler` strips internal stack traces from production HTTP responses, returning clean, standardized `ApiErrorResponse` objects with correlation IDs.
- Actuator endpoints are restricted: only `/actuator/health` and `/actuator/info` are public.

### 4.5 Cross-Site Scripting (XSS) & Header Hardening
The Spring Security Filter Chain injects standard HTTP security headers:
- `Content-Security-Policy`: Restricts scripts and styles to self.
- `X-Content-Type-Options: nosniff`.
- `X-Frame-Options: DENY`.
- `Strict-Transport-Security: max-age=31536000; includeSubDomains`.
- `Referrer-Policy: strict-origin-when-cross-origin`.
