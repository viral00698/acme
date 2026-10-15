# Test Credentials & Evaluator Guide

## 1. Authentication Credentials

The ACME Employee Salary Management System comes pre-configured with three seeded operational accounts representing the three distinct role tiers in the Role-Based Access Control (RBAC) hierarchy.

| Username | Password | Role Authority | UI Permissions & Access Scope |
| :--- | :--- | :--- | :--- |
| **`admin`** | `Admin@Acme2026!` | `ROLE_HR_ADMIN` | **Full System Authority**: Manage users, full employee CRUD, create and revise salary records, view executive analytics, export CSV reports, view system audit trail (`/audit`), manage reference tables. |
| **`manager`** | `Manager@Acme2026!` | `ROLE_HR_MANAGER` | **Operational Compensation Authority**: Search/view directory, create/edit employees, update employment status, create salary revisions with non-overlapping effective dates, view executive analytics, export CSV reports. *Forbidden from viewing audit logs or modifying security policies.* |
| **`viewer`** | `Viewer@Acme2026!` | `ROLE_HR_VIEWER` | **Read-Only / Auditor**: Search and browse employee directory, inspect employee profile and salary timeline, view dashboard analytics and compensation reports. *Action buttons hidden in UI; backend rejects all POST/PUT/DELETE mutations and CSV exports with HTTP 403 Forbidden.* |

> [!TIP]
> **One-Click Quick-Fill**: The Angular login screen (`/login`) includes one-click chip buttons (`Fill Admin`, `Fill Manager`, `Fill Viewer`) below the login form that automatically populate these test credentials for frictionless evaluator testing.

---

## 2. Seeded Dataset & Employee Code Conventions

The dataset contains **10,000 active employees** and **over 14,000 historical salary records**, generated with deterministic pseudo-random seed `42L`.

### Identification Schema
- **Employee Code Format**: `EMP` followed by 5 zero-padded digits (`EMP00001` through `EMP10000`).
- **Email Convention**: `{first_name}.{last_name}{id}@{country_domain}`
  - Example: `sarah.jenkins1@acme.com` (US)
  - Example: `oliver.smith2@acme.co.uk` (UK)
  - Example: `hans.weber5@acme.de` (Germany)
  - Example: `kenji.sato10@acme.jp` (Japan)

### Currencies and Geographical Distribution

| Country Code | Country Name | Local Currency | Standard Pay Frequency | Calibrated Salary Range (Annual) |
| :--- | :--- | :--- | :--- | :--- |
| **US** | United States | `USD` ($) | `ANNUAL` / `MONTHLY` | $55,000 – $220,000 |
| **UK** | United Kingdom | `GBP` (£) | `ANNUAL` / `MONTHLY` | £42,000 – £165,000 |
| **DE** | Germany | `EUR` (€) | `ANNUAL` / `MONTHLY` | €48,000 – €180,000 |
| **FR** | France | `EUR` (€) | `ANNUAL` / `MONTHLY` | €45,000 – €170,000 |
| **JP** | Japan | `JPY` (¥) | `ANNUAL` / `MONTHLY` | ¥6,500,000 – ¥25,000,000 |
| **CA** | Canada | `CAD` ($) | `ANNUAL` / `MONTHLY` | $65,000 – $210,000 |
| **AU** | Australia | `AUD` ($) | `ANNUAL` / `MONTHLY` | $75,000 – $230,000 |
| **IN** | India | `INR` (₹) | `ANNUAL` / `MONTHLY` | ₹1,200,000 – ₹5,500,000 |

### Specific Test Personas & Records

| Employee Code | Name | Country / Currency | Department & Job Title | Notes for Evaluator |
| :--- | :--- | :--- | :--- | :--- |
| **`EMP00001`** | First seeded record | United States (`USD`) | Engineering &middot; Software Engineer | Features 2 historical salary increases reflecting promotion timeline. |
| **`EMP00002`** | British employee | United Kingdom (`GBP`) | Product &middot; Product Manager | Active GBP salary record; non-overlapping dates. |
| **`EMP00005`** | German employee | Germany (`EUR`) | Finance &middot; Financial Analyst | Active EUR salary record; eligible for revision testing. |
| **`EMP00010`** | Japanese employee | Japan (`JPY`) | Engineering &middot; Solutions Architect | Multi-million JPY salary demonstrates zero cross-currency skew. |
| **`EMP00042`** | Multi-revision employee | Canada (`CAD`) | Sales &middot; Account Executive | 3 historical compensation entries in timeline view. |
| **`EMP10000`** | Boundary record | India (`INR`) | Operations &middot; Operations Specialist | Verifies index coverage and pagination boundary at 10,000 records. |

---

## 3. Step-by-Step Evaluator Test Scenarios

### Scenario A: Rate Limiting & Brute-Force Lockout
1. Navigate to `/login`.
2. Enter invalid credentials 5 consecutive times (e.g. `admin` / `WrongPass123!`).
3. On the 6th attempt, observe the alert message: **"Too many login attempts. Account temporarily locked for 15 minutes."**
4. Check backend response: HTTP `429 Too Many Requests`.

### Scenario B: Role-Based Authorization Restrictions
1. Log in as **`viewer`** (`Viewer@Acme2026!`).
2. Navigate to **Employee Directory** (`/employees`):
   - Notice the "+ Add Employee" button is hidden.
   - Click on an employee row: The "Edit Employee", "Change Status", and "+ Revise Salary" buttons are absent.
3. Navigate to **Salary Reports** (`/reports`):
   - Notice the "Export CSV" button is hidden.
4. Try to navigate directly to `/audit`:
   - Route guard intercepts and redirects to `/dashboard` with an authorization notice.
5. Inspect browser network console: Any manual POST/PUT/DELETE request responds with HTTP `403 Forbidden`.

### Scenario C: Effective-Dated Salary Revision & Overlap Prevention
1. Log in as **`manager`** (`Manager@Acme2026!`).
2. Open employee **`EMP00001`**.
3. Click **"+ Revise Salary"**.
4. Test Overlap Validation:
   - Select an effective start date in the past that precedes the existing active start date (or collides with a closed period).
   - Submit the dialog.
   - Observe the error banner: **"Effective date overlaps with existing salary record."** (HTTP `409 Conflict`).
5. Test Valid Revision:
   - Enter a future or current effective date (e.g., tomorrow's date).
   - Enter new salary amount: `145000.00`.
   - Change Reason: `Merit Increase`.
   - Click **"Save Revision"**.
   - Observe: The previous salary is immediately superseded with `effective_end_date` set to the day before the new start date; the new salary becomes active; the employee detail timeline displays the newly created entry.

### Scenario D: Formula-Safe CSV Export
1. Navigate to **Salary Reports** (`/reports`).
2. Filter by Currency: `USD`.
3. Filter by Department: `Engineering`.
4. Click **"Export CSV"**.
5. Save and open the generated CSV file (`acme-salary-report-*.csv`):
   - Verify all salary amounts are numeric with exact two-decimal precision.
   - Verify any fields containing potential formula trigger prefixes (`=`, `+`, `-`, `@`, `\t`, `\r`) are automatically escaped with a leading single quote (`'`), neutralizing CSV formula injection attacks in Microsoft Excel.

### Scenario E: Audit Trail Inspection
1. Log in as **`admin`** (`Admin@Acme2026!`).
2. Navigate to **Audit Trail** (`/audit`).
3. Filter by Actor: `manager` or `admin`.
4. Observe the timestamped audit logs for the salary revision created in Scenario C, showing action `UPDATE`, entity `SalaryRecord`, actor username, IP address, and JSON diff payload.
