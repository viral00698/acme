# Docker Deployment & Orchestration Guide

## 1. Overview & Architecture

The ACME Employee Salary Management System provides a production-ready, multi-container Docker deployment orchestrated via `docker-compose.yml`.

```
                    ┌────────────────────────────────────────────────────────┐
                    │                      Docker Host                       │
                    │                                                        │
                    │   ┌────────────────────────────────────────────────┐   │
   Browser Traffic  │   │          acme-salary-frontend                  │   │
  ─────────────────┼──>│               (Port 80)                        │   │
  (HTTP / Port 80)  │   │  Nginx 1.25 Alpine + Angular 19 SPA            │   │
                    │   └───────────────────────┬────────────────────────┘   │
                    │                           │ /api/* reverse proxy       │
                    │                           ▼                            │
                    │   ┌────────────────────────────────────────────────┐   │
                    │   │          acme-salary-backend                   │   │
                    │   │              (Port 8080)                       │   │
                    │   │  Spring Boot 3 + Eclipse Temurin 17 JRE        │   │
                    │   └───────────────────────┬────────────────────────┘   │
                    │                           │ JDBC (5432)                │
                    │                           ▼                            │
                    │   ┌────────────────────────────────────────────────┐   │
                    │   │          acme-salary-postgres                  │   │
                    │   │              (Port 5432)                       │   │
                    │   │  PostgreSQL 16 Alpine + Persistent Volume      │   │
                    │   └────────────────────────────────────────────────┘   │
                    └────────────────────────────────────────────────────────┘
```

---

## 2. Prerequisites

| Requirement | Minimum Version | Verification |
| :--- | :--- | :--- |
| **Docker Engine** | 20.10+ | `docker --version` |
| **Docker Compose** | Compose v2 (v2.20+) | `docker compose version` |
| **System Memory** | Minimum 4 GB RAM available | |
| **Available Ports** | 80, 8080, 5432 | `netstat -ano \| findstr :80` |

---

## 3. Quickstart Deployment

To launch the complete application stack with automated database initialization, migration, and deterministic 10,000 employee data seeding:

```bash
# 1. From repository root directory
docker compose up --build -d
```

### Cascading Healthcheck Sequence
The startup lifecycle executes with deterministic ordering:
1. **`postgres` container starts first**: Initialized with database `acme_salary_db`. Docker monitors `pg_isready -U postgres -d acme_salary_db` until status is `healthy`.
2. **`backend` container boots**: Starts only after `postgres` is healthy.
   - Flyway applies schema `V1` and reference data `V2`.
   - `DataSeederRunner` populates **10,000 employees** and **14,000+ salary records**.
   - Docker monitors `curl -f http://localhost:8080/actuator/health` until status is `healthy`.
3. **`frontend` container boots**: Starts only after `backend` is healthy. Nginx launches on port 80 and serves the compiled Angular application while proxying `/api` traffic.

---

## 4. Application Endpoints

Once all three containers are healthy:

| Service / Interface | URL | Purpose |
| :--- | :--- | :--- |
| **ACME Web Application** | **[http://localhost](http://localhost)** | Main responsive UI (port 80) |
| **Backend API Root** | [http://localhost:8080/api/v1](http://localhost:8080/api/v1) | Direct Spring Boot REST API |
| **Swagger UI Documentation** | [http://localhost/swagger-ui/index.html](http://localhost/swagger-ui/index.html) | Interactive OpenAPI 3.0 explorer |
| **Actuator Health Probe** | [http://localhost/actuator/health](http://localhost/actuator/health) | System readiness and health check |

---

## 5. Container Management & Monitoring

### Check Container Status and Health
```bash
docker compose ps
```
*Expected Output:*
```text
NAME                   IMAGE                         STATUS                    PORTS
acme-salary-postgres   postgres:16-alpine            Up (healthy)              0.0.0.0:5432->5432/tcp
acme-salary-backend    employee-backend              Up (healthy)              0.0.0.0:8080->8080/tcp
acme-salary-frontend   employee-frontend             Up (healthy)              0.0.0.0:80->80/tcp
```

### Tail Live Logs
```bash
# All services
docker compose logs -f

# Backend service logs
docker compose logs -f backend

# Database logs
docker compose logs -f postgres
```

### Stopping the Stack
```bash
# Graceful stop (preserves seeded database volume)
docker compose down

# Stop and wipe volume (resets database to clean slate for fresh re-seed)
docker compose down -v
```

---

## 6. Container Hardening & Security Features

- **Non-Root Execution**: The backend container runs under unprivileged system user `appuser` (UID 1000) belonging to `appgroup`.
- **Minimal Attack Surface**: Uses Alpine Linux bases for all stages (`node:20-alpine`, `nginx:alpine`, `eclipse-temurin:17-jre-alpine`, `postgres:16-alpine`).
- **Container-Aware JVM Tuning**: Enabled `-XX:+UseContainerSupport` and `-XX:MaxRAMPercentage=75.0` to prevent OOM kills within container memory limits.
- **Edge Nginx Security**: Reverse proxy automatically injects security headers (`X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `CSP`, and `Referrer-Policy`).
- **Data Persistence**: Named volume `postgres_data` guarantees durability of transactional salary adjustments across container restarts.
