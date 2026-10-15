# Low-Level Design (LLD): Modules D & E — Analytics & Reports

## 1. Responsibilities
- Real-time aggregation of compensation metrics across 10,000 employees.
- Computing active headcounts by country and department.
- Calculating min, max, and average annualized salary strictly grouped by currency.
- Generating salary band distributions.
- Generating tabular HR reports with multi-criteria filters.
- Generating streaming CSV exports with formula-injection defenses.

## 2. Main Classes & Interfaces
- `com.acme.salary.analytics.controller.AnalyticsController`: Dashboard statistics endpoints.
- `com.acme.salary.analytics.service.AnalyticsService`: Computes SQL aggregate projections.
- `com.acme.salary.reports.controller.ReportController`: Tabular reports and CSV export.
- `com.acme.salary.reports.service.ReportService`: Handles filtered report pagination and CSV streaming.
- `com.acme.salary.reports.util.CsvSanitizer`: Prepends `'` to fields starting with `=, +, -, @, \t, \r`.

## 3. Data Transfer Objects (DTOs)
- `DashboardOverviewDto`:
  - `totalActiveEmployees`: number
  - `totalDepartments`: number
  - `totalCountries`: number
  - `currencySummaries`: `CurrencySalarySummaryDto[]`
- `CurrencySalarySummaryDto`:
  - `currencyCode`: string
  - `currencySymbol`: string
  - `employeeCount`: number
  - `minSalary`: BigDecimal
  - `maxSalary`: BigDecimal
  - `avgSalary`: BigDecimal
- `DepartmentAnalyticsDto`: `{ departmentId, departmentName, employeeCount, avgSalaryByCurrency: Map<String, BigDecimal> }`
- `CountryAnalyticsDto`: `{ countryId, countryName, countryCode, employeeCount, avgSalaryByCurrency: Map<String, BigDecimal> }`
- `SalaryBandDto`: `{ bandName: string, minAmount: BigDecimal, maxAmount: BigDecimal, currencyCode: string, employeeCount: number }`
- `SalaryReportRowDto`: `{ employeeCode, employeeName, department, country, jobTitle, status, salaryAmount, currency, payFrequency, effectiveFrom }`

## 4. API Contract

### `GET /api/v1/analytics/overview`
- **Response**: `200 OK` -> `DashboardOverviewDto`.
- **Authorization**: `HR_ADMIN`, `HR_MANAGER`, `HR_VIEWER`.

### `GET /api/v1/analytics/by-country`
- **Response**: `200 OK` -> `List<CountryAnalyticsDto>`.

### `GET /api/v1/analytics/by-department`
- **Response**: `200 OK` -> `List<DepartmentAnalyticsDto>`.

### `GET /api/v1/analytics/salary-distribution`
- **Query Params**: `currency` (required, e.g. `USD`).
- **Response**: `200 OK` -> `List<SalaryBandDto>`.

### `GET /api/v1/reports/salaries`
- **Query Params**: `countryId`, `departmentId`, `currency`, `minSalary`, `maxSalary`, `status`, `page`, `size`.
- **Response**: `200 OK` -> `PageResponse<SalaryReportRowDto>`.

### `GET /api/v1/reports/salaries/export`
- **Query Params**: Same as report endpoint.
- **Response**: `200 OK` -> `Content-Type: text/csv`, streaming response with sanitized values.
- **Authorization**: `HR_ADMIN`, `HR_MANAGER`.

## 5. Frequency Normalization Formula
When comparing salaries with differing frequencies:
- `ANNUAL`: `Amount * 1.0`
- `MONTHLY`: `Amount * 12.0`
- `BIWEEKLY`: `Amount * 26.0`
All aggregations explicitly state whether the metric is annualized.
