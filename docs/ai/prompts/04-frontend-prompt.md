# AI Prompt 04: Frontend Application & Responsive UI Design

## Objective
Build a professional, responsive, and robust Angular frontend application for the ACME Employee Salary Management System using Angular 19/22, Angular Material, Angular Signals, and Reactive Forms.

## Requirements
1. **Core Infrastructure**:
   - Centralized authentication service with JWT token lifecycle management, refresh token rotation, and in-memory/localStorage session handling.
   - Functional HTTP interceptor injecting Bearer tokens and automatically resolving 401 Unauthorized via refresh endpoint.
   - Route guards for authentication (`authGuard`) and role-based permissions (`roleGuard`).
   - Domain models reflecting backend DTOs with strict TypeScript interfaces.
2. **Layout & Navigation**:
   - Modern enterprise shell with responsive collapsible navigation drawer, top toolbar, ACME branding, active route highlighting, and user profile chip with dynamic role badge.
3. **Authentication Screens**:
   - Elegant login card with reactive validation, rate-limit alert rendering, and quick-fill evaluation preset buttons (`admin`, `manager`, `viewer`).
4. **Dashboard**:
   - Executive compensation overview with KPI metric cards (Headcount, Departments, Countries, Currencies).
   - Single-currency segregation cards displaying Min, Avg, Max, and Total Spend per currency.
   - Interactive salary band distribution histogram with currency selector.
   - Department and country workforce distribution tables.
5. **Employee Directory**:
   - Server-side paginated and sortable employee table.
   - Multi-criteria filter form (search debounce, department, country, status).
   - Create and Edit employee reactive dialog with dynamic job title filtering by department.
   - Status change dialog with change notes.
   - Employee profile details view with active compensation card.
