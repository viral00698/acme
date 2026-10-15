# Prompt 05 — Containerization, DevOps, and Deployment

## Objective
Act as a DevOps Engineer and Systems Architect. Create a production-ready, multi-stage Docker deployment setup for the ACME Employee Salary Management System.

## Mandatory Specifications
1. **Spring Boot Backend Container (`backend/Dockerfile`)**:
   - Multi-stage build (`maven:3.9.9-eclipse-temurin-17-alpine` builder + `eclipse-temurin:17-jre-alpine` runtime).
   - Dedicated unprivileged system user (`appuser:appgroup`).
   - JVM container-aware memory flags (`-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`).
   - Spring Boot Actuator health check on `/actuator/health`.
   - Dedicated `.dockerignore`.
2. **Angular Frontend Container (`frontend/Dockerfile`)**:
   - Multi-stage build (`node:20-alpine` builder + `nginx:alpine` runtime).
   - Custom `nginx.conf`:
     - Gzip compression for static assets and API JSON responses.
     - Reverse proxy routing for `/api/`, `/swagger-ui/`, `/v3/api-docs/`, and `/actuator/health`.
     - Security headers: `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, CSP.
     - Single Page Application (SPA) HTML5 pushState fallback (`try_files $uri $uri/ /index.html`).
   - Dedicated `.dockerignore`.
3. **Container Orchestration (`docker-compose.yml`)**:
   - PostgreSQL 16 Alpine with named volume persistence `postgres_data` and `pg_isready` health check.
   - Spring Boot backend container with environment-based configuration and dependency on healthy `postgres`.
   - Angular / Nginx frontend gateway container with dependency on healthy `backend`.
   - Root `.dockerignore`.
