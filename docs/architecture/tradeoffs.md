# Architectural Tradeoffs & Decisions — ACME Employee Salary Management System

## 1. Modular Monolith vs. Microservices Architecture
- **Decision**: Adopt a well-structured **Modular Monolith** in Spring Boot 3.3.4 rather than microservices (e.g. Employee Service, Salary Service, Analytics Service).
- **Alternatives Considered**: Event-driven microservices using Spring Cloud, Kafka, and separate databases.
- **Tradeoff Analysis**:
  - *Microservices Pros*: Independent deployment cycles, per-service technology choices.
  - *Microservices Cons*: Two-phase commits (2PC) or eventual consistency required for salary revisions; distributed transaction failure modes; high operational overhead for Docker Compose; network serialization latency.
  - *Monolith Pros*: Strong ACID guarantees for financial changes; single atomic transactions across salary records and audit logs; zero network hop latency; trivial local reproducibility.
- **Conclusion**: For 10,000 employees and an HR team of ~100 users, a modular monolith provides maximum data integrity, zero eventual consistency risks, and optimal developer ergonomics.

## 2. Effective-Dated Salary Records vs. Mutable Current Salary Column
- **Decision**: Model compensation strictly as immutable, effective-dated records (`effective_from`, `effective_to`) in `salary_records`, with current salary defined as `effective_to IS NULL`. No redundant `current_salary` column on the `Employee` entity.
- **Alternatives Considered**: Storing `current_salary` directly on `employees` and maintaining a separate `salary_history` log.
- **Tradeoff Analysis**:
  - *Denormalized Employee Column*: Slightly simpler query for current salary, but introduces double-write hazards, synchronization bugs, and potential divergence between the employee row and the audit log.
  - *Pure Effective Dating*: Requires a partial index or `effective_to IS NULL` check, but provides a 100% mathematically consistent audit trail and enables future-dated salary scheduling.
- **Conclusion**: Pure effective dating ensures audit integrity and eliminates desynchronization bugs.

## 3. Server-Side SQL Aggregation vs. Client-Side Analytics
- **Decision**: Compute all summary analytics, salary bands, and department averages inside PostgreSQL using optimized `GROUP BY` queries.
- **Alternatives Considered**: Fetching large employee JSON arrays into Angular and computing metrics via JavaScript array operations.
- **Tradeoff Analysis**:
  - *Client-Side*: Would require transferring 10,000 employee objects (~10MB payload) across the network to every browser, causing UI freezing, high mobile memory consumption, and sluggish dashboard loads.
  - *Server-Side*: Transfers < 10KB of aggregated summary JSON; executes in < 15ms inside PostgreSQL's vectorized query engine; supports low-bandwidth mobile devices.
- **Conclusion**: Server-side aggregation is non-negotiable for 10,000 records.

## 4. Soft Deactivation vs. Hard Foreign-Key Deletion
- **Decision**: Implement employee termination via status transitions (`ACTIVE` -> `INACTIVE` / `TERMINATED`). Disallow hard SQL `DELETE` for employees with salary records or audit entries.
- **Alternatives Considered**: Cascading foreign-key delete (`ON DELETE CASCADE`).
- **Tradeoff Analysis**:
  - *Hard Deletion*: Permanently destroys historical compensation records, violating labor law compliance, tax auditability, and relational integrity.
  - *Soft Deactivation*: Preserves historical fidelity while filtering out inactive employees from active headcount analytics.
- **Conclusion**: Soft deactivation is mandatory for HR compliance.

## 5. Currency-Segregated Analytics vs. Synthetic FX Conversions
- **Decision**: Partition all compensation aggregations strictly by ISO currency code. Cross-currency comparisons are explicitly disallowed unless partitioned.
- **Alternatives Considered**: Converting all salaries to USD using mock exchange rates.
- **Tradeoff Analysis**:
  - *Automated FX Conversion*: Unreliable without live, audited market rates and creates false financial equivalences across varying regional cost-of-living bases.
  - *Segregated Grouping*: Reflects true financial reality and prevents HR errors (e.g., comparing 100,000 USD to 100,000 JPY or 1,000,000 INR).
- **Conclusion**: Grouping by currency delivers reliable, audit-proof compensation insights.
