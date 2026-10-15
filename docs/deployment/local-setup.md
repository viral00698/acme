# Local Development Setup Guide

## 1. System Prerequisites

Ensure the following tools are installed on your host machine before starting:

| Tool | Minimum Version | Recommended Version | Verification Command |
| :--- | :--- | :--- | :--- |
| **Java Development Kit (JDK)** | Java 17 | Eclipse Temurin 17.0.12+ | `java -version` |
| **Node.js** | Node 18.19.0+ | Node 20.x LTS | `node -v` |
| **npm** | 10.x+ | 10.8.x+ | `npm -v` |
| **PostgreSQL** | 15+ | PostgreSQL 16.x | `psql -V` |
| **Git** | 2.40+ | Latest stable | `git --version` |

---

## 2. Database Provisioning

1. Connect to your local PostgreSQL instance:
   ```bash
   psql -U postgres -h localhost -p 5432
   ```
2. Create the application database:
   ```sql
   CREATE DATABASE acme_salary_db;
   ```
3. Ensure user credentials match standard development settings (`postgres` / `root`) or set your environment variables accordingly:
   ```bash
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=acme_salary_db
   export DB_USER=postgres
   export DB_PASSWORD=root
   ```

---

## 3. Backend Setup (Spring Boot 3)

The backend includes a Maven wrapper (`mvnw` on Linux/macOS, `mvnw.cmd` on Windows), so a pre-installed standalone Maven installation is optional.

### Configuration
Backend configuration is managed via `backend/src/main/resources/application.yml` and environment profiles (`dev`, `prod`, `test`). Key environment variables:

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile |
| `DB_HOST` | `localhost` | PostgreSQL host address |
| `DB_PORT` | `5432` | PostgreSQL listening port |
| `DB_NAME` | `acme_salary_db` | Target database catalog |
| `DB_USER` | `postgres` | Database connection role |
| `DB_PASSWORD` | `root` | Database connection password |
| `JWT_SECRET` | *(64-character hex key)* | HMAC-SHA256 signing secret |
| `SEED_INITIAL_DATA` | `true` | Automatically seeds 10k employees on boot if empty |

### Build & Run
From the repository root:
```bash
# Windows
cd backend
.\mvnw.cmd clean spring-boot:run

# Linux / macOS
cd backend
./mvnw clean spring-boot:run
```

### Startup Lifecycle
1. **Flyway Migrations**: Automatically applies `V1__init_schema.sql` (tables, constraints, composite indexes) and `V2__seed_reference_data.sql` (departments, countries, currencies, job titles, and base users).
2. **Deterministic Data Seeder**: `DataSeederRunner` detects whether `employees` has fewer than 10,000 records. If empty, it executes high-speed two-phase batch inserts generating **10,000 employees** and **over 14,000 salary records** in approximately 3.2 seconds.
3. **HTTP Server**: Binds to port `8080`.
   - Swagger OpenAPI UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - Actuator Health Check: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 4. Frontend Setup (Angular 19 / Angular Material)

The frontend is an Angular Single Page Application configured with Angular Material and reactive signals.

### Installation
From the repository root:
```bash
cd frontend
npm install
```

### Running with Reverse Proxy
Launch the Angular development server using the pre-configured backend proxy (`proxy.conf.json`):
```bash
npm start
# or explicitly: npx ng serve --proxy-config proxy.conf.json
```
The application will launch and be available at:
👉 **[http://localhost:4200](http://localhost:4200)**

All requests to `/api/*` are transparently proxied to `http://localhost:8080/api/*`, eliminating CORS issues during development.

---

## 5. Verification & Testing

### Running Backend Tests
Execute the complete test suite (unit tests, integration tests, security authorization matrices, rate limiting lockout, and overlap checks):
```bash
cd backend
.\mvnw.cmd test
```
*Expected: 64 tests run, 0 failures, 0 errors.*

### Running Frontend Tests
Execute the Vitest test suite covering services, guards, and components:
```bash
cd frontend
npm test -- --watch=false
```
*Expected: 7 test files, 22 tests passing.*

### Running Production Bundle Build
Verify the Ahead-of-Time (AOT) production bundle compiles cleanly within budget constraints:
```bash
cd frontend
npm run build
```
