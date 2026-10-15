# Scalability & Performance Strategy — ACME Employee Salary Management System

## 1. Scale Target & Workload Profile
The application is engineered to comfortably support 10,000 employees with multi-year salary revisions (estimated 25,000 to 50,000 `salary_records`), accessed concurrently by up to 100 HR operators.

### Target Performance SLA (p95)
- Paginated employee list (20 items): **< 100 ms**
- Full-text search by name/code across 10,000 rows: **< 120 ms**
- Salary dashboard multi-country/multi-department aggregation: **< 200 ms**
- Salary revision transaction commit (including audit write): **< 80 ms**
- CSV report generation (up to 10,000 rows): **< 1500 ms** (streamed)

## 2. Relational Query Optimization & Indexing

### 2.1 Indexing for Fast Query Paths
- **Sequential Scan Elimination**: Full scans on 10,000 employee records take ~30-50ms of CPU time in Postgres. Using B-tree indexes reduces point lookups and paginated range scans to < 2ms.
- **Covering Indexes**: Queries for directory listings utilize composite indexes `(department_id, country_id, status)` so Postgres can satisfy filter conditions directly in the index index-scan phase.
- **Partial Indexing for Active Salaries**:
  ```sql
  CREATE INDEX idx_salary_active ON salary_records(employee_id) WHERE effective_to IS NULL;
  ```
  Only 10,000 records out of 50,000 total historical records are active. This partial index occupies minimal memory and accelerates the critical path of fetching current compensation.

### 2.2 Avoiding the N+1 Problem
- Hibernate relationships (`@ManyToOne` from Employee to Department, Country, JobTitle) are explicitly fetched using `JOIN FETCH` or `EntityGraph` in repository queries.
- Direct JSON serialization of entities is prohibited; DTO projections prevent accidental lazy-loading loops outside transaction boundaries.

## 3. Memory & Stream Management

### 3.1 Bounded Pagination
- Unbounded queries (`SELECT * FROM employees`) are rejected by controller validation.
- All list endpoints enforce `page >= 0` and `size <= 100` (default 20).
- Deep pagination queries use index-backed sort keys (`ORDER BY id ASC/DESC` or `last_name, id`).

### 3.2 Streaming CSV Export
- Exporting 10,000 employee records as a single in-memory `byte[]` or `String` could consume 15-20MB of heap per export, creating GC pressure under concurrent requests.
- Solution: The export service reads records in chunks via Spring Data JPA `Stream<EmployeeSalaryReportRow>` and writes directly to the servlet output stream using `StreamingResponseBody`. Rows are flushed iteratively, maintaining heap consumption below 2MB regardless of dataset size.

## 4. Connection Pooling & Database Configuration
- **HikariCP**: Default connection pool configured with:
  - `maximum-pool-size: 20`
  - `minimum-idle: 10`
  - `connection-timeout: 20000ms`
  - `idle-timeout: 300000ms`
  - `max-lifetime: 1200000ms`
- Sufficient to handle ~500 requests per second given average query latencies of 5-15ms.

## 5. Future Scalability Roadmap (100,000+ Employees)
While the modular monolith easily satisfies the current 10,000 employee requirement, future scale can be achieved via:
1. **Read Replicas**: Routing read-only analytics and reports to a PostgreSQL read replica via Spring `AbstractRoutingDataSource`.
2. **Distributed Caching**: Introducing Redis for caching immutable reference data (departments, countries, currencies) and hourly dashboard aggregation summaries.
3. **Background Asynchronous Reporting**: Offloading very large enterprise-wide exports to Spring Batch / asynchronous worker queues with S3 presigned download links.
