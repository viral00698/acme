# Relational Database Query Analysis & Execution Plans

## 1. Index Strategy & Schema Architecture

The relational schema is indexed specifically to satisfy high-volume search, pagination, effective-dated timeline lookups, and single-currency executive aggregations across **10,000+ employees**.

```
employees
 ├── employees_pkey [id]
 ├── employees_employee_code_key UNIQUE [employee_code]
 ├── employees_email_key UNIQUE [email]
 ├── idx_employees_code [employee_code]
 ├── idx_employees_names [last_name, first_name]
 ├── idx_employees_dept_country_status [department_id, country_id, status]
 └── idx_employees_hire_date [hire_date]

salary_records
 ├── salary_records_pkey [id]
 ├── uq_employee_active_salary UNIQUE [employee_id] WHERE effective_to IS NULL
 ├── idx_salary_currency_active [currency_code] WHERE effective_to IS NULL
 ├── idx_salary_employee_history [employee_id, effective_from, effective_to]
 └── idx_salary_effective_from [effective_from]
```

---

## 2. Query 1: Paginated Directory with Active Salary Join

### SQL Statement
```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT e.id, e.employee_code, e.first_name, e.last_name, e.email,
       d.name, c.name, s.amount, s.currency_code
FROM employees e
JOIN departments d ON d.id = e.department_id
JOIN countries c ON c.id = e.country_id
JOIN salary_records s ON s.employee_id = e.id
WHERE s.effective_to IS NULL
  AND e.department_id = 1
  AND e.status = 'ACTIVE'
ORDER BY e.last_name ASC
LIMIT 20;
```

### PostgreSQL Execution Plan
```text
 Limit  (cost=0.87..43.30 rows=20 width=509) (actual time=0.142..0.796 rows=20 loops=1)
   Buffers: shared hit=348
   ->  Nested Loop  (cost=0.87..2440.49 rows=1150 width=509) (actual time=0.141..0.794 rows=20 loops=1)
         Buffers: shared hit=348
         ->  Nested Loop  (cost=0.59..1165.12 rows=1150 width=498) (actual time=0.120..0.639 rows=20 loops=1)
               Buffers: shared hit=288
               ->  Nested Loop  (cost=0.43..1134.58 rows=1150 width=288) (actual time=0.106..0.615 rows=20 loops=1)
                     Buffers: shared hit=286
                     ->  Index Scan using idx_employees_names on employees e  (cost=0.29..1112.04 rows=1150 width=78) (actual time=0.056..0.557 rows=20 loops=1)
                           Filter: ((department_id = 1) AND ((status)::text = 'ACTIVE'::text))
                           Rows Removed by Filter: 260
                           Buffers: shared hit=281
                     ->  Materialize  (cost=0.14..8.17 rows=1 width=226) (actual time=0.002..0.003 rows=1 loops=20)
                           Buffers: shared hit=5
                           ->  Index Scan using departments_pkey on departments d  (cost=0.14..8.16 rows=1 width=226) (actual time=0.045..0.045 rows=1 loops=1)
                                 Index Cond: (id = 1)
                                 Buffers: shared hit=5
               ->  Memoize  (cost=0.16..0.22 rows=1 width=226) (actual time=0.001..0.001 rows=1 loops=20)
                     Cache Key: e.country_id
                     Cache Mode: logical
                     Hits: 19  Misses: 1  Evictions: 0  Overflows: 0  Memory Usage: 1kB
                     Buffers: shared hit=2
                     ->  Index Scan using countries_pkey on countries c  (cost=0.15..0.21 rows=1 width=226) (actual time=0.010..0.010 rows=1 loops=1)
                           Index Cond: (id = e.country_id)
                           Buffers: shared hit=2
         ->  Index Scan using uq_employee_active_salary on salary_records s  (cost=0.29..1.10 rows=1 width=19) (actual time=0.007..0.007 rows=1 loops=20)
               Index Cond: (employee_id = e.id)
               Buffers: shared hit=60
 Planning Time: 3.428 ms
 Execution Time: 0.920 ms
```

### Plan Insights
- **Execution Time**: **0.920 ms** (sub-millisecond).
- **Zero Disk I/O**: `shared hit=348, read=0` (100% buffer cache hits).
- **Index Synergy**: `idx_employees_names` fulfills the alphabetical ordering on `last_name ASC`, while the partial unique index `uq_employee_active_salary` retrieves the active salary in **0.007 ms** per loop without scanning historical salary rows.

---

## 3. Query 2: Executive Currency Aggregation

### SQL Statement
```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT currency_code,
       COUNT(*),
       MIN(amount),
       MAX(amount),
       AVG(amount),
       SUM(amount)
FROM salary_records
WHERE effective_to IS NULL
GROUP BY currency_code;
```

### PostgreSQL Execution Plan
```text
 HashAggregate  (cost=484.78..484.90 rows=8 width=140) (actual time=9.512..9.517 rows=8 loops=1)
   Group Key: currency_code
   Batches: 1  Memory Usage: 24kB
   Buffers: shared hit=199
   ->  Seq Scan on salary_records  (cost=0.00..359.78 rows=10000 width=11) (actual time=0.023..4.332 rows=10000 loops=1)
         Filter: (effective_to IS NULL)
         Rows Removed by Filter: 6078
         Buffers: shared hit=199
 Planning Time: 1.330 ms
 Execution Time: 9.694 ms
```

### Plan Insights
- **Execution Time**: **9.694 ms** across all 10,000 active records.
- **Single-Currency Invariant**: The query groups strictly by `currency_code`, guaranteeing that currencies (e.g. `USD`, `EUR`, `JPY`) are calculated in pure, isolated silos.
- **Memory Footprint**: Hash aggregation requires only 24 kB of RAM.

---

## 4. Query 3: Point Lookup by Employee Code

### SQL Statement
```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, employee_code, first_name, last_name, email
FROM employees
WHERE employee_code = 'EMP00042';
```

### PostgreSQL Execution Plan
```text
 Index Scan using idx_employees_code on employees  (cost=0.29..8.30 rows=1 width=62) (actual time=0.033..0.033 rows=1 loops=1)
   Index Cond: ((employee_code)::text = 'EMP00042'::text)
   Buffers: shared hit=2
 Planning Time: 0.955 ms
 Execution Time: 0.054 ms
```

### Plan Insights
- **Execution Time**: **0.054 ms** (54 microseconds).
- **Buffer Hits**: Exactly 2 buffer page lookups (root B-tree node and leaf page).

---

## 5. Query 4: Chronological Salary History Timeline

### SQL Statement
```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, employee_id, amount, currency_code, pay_frequency, effective_from, effective_to
FROM salary_records
WHERE employee_id = 42
ORDER BY effective_from DESC;
```

### PostgreSQL Execution Plan
```text
 Index Scan Backward using idx_salary_employee_history on salary_records  (cost=0.29..10.07 rows=2 width=42) (actual time=0.030..0.032 rows=2 loops=1)
   Index Cond: (employee_id = 42)
   Buffers: shared hit=7
 Planning Time: 0.759 ms
 Execution Time: 0.112 ms
```

### Plan Insights
- **Execution Time**: **0.112 ms**.
- **Backward Index Scan**: Reuses `idx_salary_employee_history` in reverse order (`DESC`), eliminating sort operations and temporary disk writes entirely.
