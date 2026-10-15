# Low-Level Design (LLD): Module A — Authentication & Authorization

## 1. Responsibilities
- Secure credential verification via BCrypt.
- Issuance and verification of signed stateless JWT access tokens (15-minute validity).
- Cryptographic refresh token issuance, persistence (SHA-256 hashed), rotation, and revocation.
- Current authenticated user resolution (`/api/v1/auth/me`).
- Granular Role-Based Access Control (`HR_ADMIN`, `HR_MANAGER`, `HR_VIEWER`).
- In-memory login rate limiting and brute-force mitigation.

## 2. Main Classes & Interfaces
- `com.acme.salary.auth.controller.AuthController`: Exposes REST endpoints.
- `com.acme.salary.auth.service.AuthService`: Orchestrates authentication, token generation, refresh, and logout.
- `com.acme.salary.auth.service.RefreshTokenService`: Manages refresh token lifecycle and hashing.
- `com.acme.salary.auth.security.JwtTokenProvider`: Encapsulates HMAC-SHA256 signing, claims parsing, and validation.
- `com.acme.salary.auth.security.JwtAuthenticationFilter`: Per-request filter extracting Bearer token and populating `SecurityContextHolder`.
- `com.acme.salary.auth.security.SecurityConfig`: Spring Security 6 filter chain configuration, CORS, session management, and URL authorization rules.
- `com.acme.salary.auth.security.LoginRateLimitService`: Sliding window brute-force blocker.

## 3. Data Transfer Objects (DTOs)
- `LoginRequest`: `{ username: string (NotBlank), password: string (NotBlank) }`
- `RefreshTokenRequest`: `{ refreshToken: string (NotBlank) }`
- `AuthResponse`: `{ accessToken: string, refreshToken: string, tokenType: "Bearer", expiresIn: number, user: UserProfileDto }`
- `UserProfileDto`: `{ id: number, username: string, email: string, roles: string[] }`

## 4. API Contract

### `POST /api/v1/auth/login`
- **Request**: `LoginRequest`
- **Response**: `200 OK` -> `AuthResponse`
- **Errors**: `400 Bad Request` (validation failed), `401 Unauthorized` (bad credentials), `429 Too Many Requests` (rate limited).

### `POST /api/v1/auth/refresh`
- **Request**: `RefreshTokenRequest`
- **Response**: `200 OK` -> `AuthResponse` (rotated token pair)
- **Errors**: `401 Unauthorized` (token expired, revoked, or invalid).

### `POST /api/v1/auth/logout`
- **Request**: `RefreshTokenRequest`
- **Response**: `204 No Content`
- **Behavior**: Revokes refresh token in database and clears SecurityContext.

### `GET /api/v1/auth/me`
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `200 OK` -> `UserProfileDto`
- **Errors**: `401 Unauthorized`.

## 5. Security Invariants
- Passwords are validated using `BCryptPasswordEncoder(12)`.
- Access tokens cannot be revoked individually; their lifespan is capped at 15 minutes.
- Refresh tokens are single-use (rotated upon every refresh). Any reuse of an old refresh token indicates possible theft and invalidates the session family.
