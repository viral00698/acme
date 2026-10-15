# AI Prompt 03 — Module A: Authentication & Role-Based Access Control

```markdown
Implement the complete production-grade authentication and authorization engine for the ACME Employee Salary Management System:
- Entities: UserAccount, Role, RefreshToken
- Repositories with custom lookup queries
- Spring Security 6 filter chain with Stateless Session Management and custom AuthenticationEntryPoint / AccessDeniedHandler
- JwtTokenProvider with HMAC-SHA256 signature validation and claims parsing
- RefreshTokenService with SHA-256 hashed database storage and automatic token rotation
- LoginRateLimitService for brute-force protection
- GlobalExceptionHandler with standardized ApiErrorResponse DTOs
- REST AuthController (/api/v1/auth/login, /refresh, /logout, /me)
- Unit and MockMvc integration tests verifying credentials, token rotation, and RBAC enforcement.
```
