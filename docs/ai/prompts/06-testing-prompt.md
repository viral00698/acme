# Prompt 06 — Testing, Security Verification, and Evaluator Experience

## Objective
Act as a QA Lead and Application Security Engineer. Establish complete testing coverage, test cases, and credentials documentation for the ACME Employee Salary Management System.

## Mandatory Specifications
1. **Testing Strategy & Matrix**:
   - Comprehensive test cases matrix covering authentication, rate limiting lockout, RBAC role hierarchy (`HR_ADMIN`, `HR_MANAGER`, `HR_VIEWER`), employee CRUD, effective dating overlap rules, single-currency executive analytics, formula-safe CSV export, and audit logging.
2. **Test Credentials & Walkthrough**:
   - Documented credentials table for all roles.
   - Quick-fill test buttons on login interface.
   - Employee identification conventions and key test personas across the 10,000 seeded dataset (`EMP00001` through `EMP10000`).
   - Detailed step-by-step evaluator walkthrough scenarios.
3. **Empirical Benchmarks & Execution Analysis**:
   - PostgreSQL `EXPLAIN (ANALYZE, BUFFERS)` execution plans on key queries over 10,000 employees.
   - Latency percentiles (p50, p95, p99) and memory metrics.
4. **Verification Pass**:
   - Backend test suite execution (`mvnw test`).
   - Frontend test suite execution (`npm test`).
   - Production bundle validation (`npm run build`).
