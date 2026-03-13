# t31 DevOps Artifact — Production Docker Deployment

**Task:** t31 | **Role:** devops | **Status:** ✅ COMPLETE  
**Date:** 2026-03-13

---

## Scope Assessment

[scope] Production Docker Compose + Dockerfiles + Nginx reverse proxy + env docs (files: 7 created, subsystems: 2 — backend/frontend). Within limits.

---

## Deliverables

| File | Purpose |
|---|---|
| `backend/Dockerfile` | Multi-stage Maven → JRE 21 (Alpine) |
| `backend/.dockerignore` | Exclude target/, IDE files |
| `frontend/Dockerfile` | Multi-stage Node 20 build → Nginx 1.27 (Alpine) |
| `frontend/nginx.conf` | SPA server + `/api/` reverse proxy |
| `frontend/.dockerignore` | Exclude node_modules, dist, .env |
| `docker-compose.yml` | 3-service stack: db + backend + frontend |
| `.env.example` | All environment variable documentation |

---

## Architecture

```
                    ┌─────────────────────────────────┐
                    │  frontend container (Nginx)       │
Internet :80 ──────▶│  ├─ static: /usr/share/nginx/html│
                    │  ├─ /api/*  → proxy backend:8080  │
                    │  └─ /actuator/* → proxy backend   │
                    └───────────────┬─────────────────┘
                                    │ backend-net
                    ┌───────────────▼─────────────────┐
                    │  backend container (Spring Boot)  │
                    │  port 8080, context-path /api     │
                    │  Flyway V0-V3 runs on startup     │
                    └───────────────┬─────────────────┘
                                    │ backend-net
                    ┌───────────────▼─────────────────┐
                    │  db container (MySQL 8.0)         │
                    │  volume: db-data                  │
                    └─────────────────────────────────┘
```

**Key design decisions:**
- Single Nginx entry point on port 80 — frontend is embedded in the Nginx image (no separate frontend container)
- Backend is never exposed to the host network (no `ports:` binding on the backend service)
- MySQL is also internal-only — remove the `ports:` binding on `db` in production
- `resolver 127.0.0.11` in Nginx defers `backend` hostname resolution to runtime, enabling graceful backend restarts without nginx reload
- Flyway V0-V3 auto-applies on backend startup (no manual migration step)

---

## Quick Start

```bash
# 1. Copy and fill in secrets
cp .env.example .env
# Edit .env — set MYSQL_ROOT_PASSWORD, MYSQL_PASSWORD, JWT_SECRET

# 2. Build images and start
docker compose up -d --build

# 3. Verify
curl http://localhost/actuator/health   # → {"status":"UP"}
curl http://localhost/api/auth/login \  # → JWT token
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

---

## Backend Dockerfile Details

```
Stage 1 (build):  maven:3.9-eclipse-temurin-21-alpine
  - mvn dependency:go-offline  ← cached layer (only re-run if pom.xml changes)
  - mvn package -DskipTests    ← produces target/system-backend-1.0.0-SNAPSHOT.jar

Stage 2 (runtime): eclipse-temurin:21-jre-alpine
  - Non-root user: appuser:appgroup
  - Upload dir: /var/app/uploads  (mounted as Docker volume)
  - ENTRYPOINT: sh -c "exec java $JAVA_OPTS -jar /app/app.jar"
  - EXPOSE: 8080
```

**Spring Boot property overrides via environment variables** (RelaxedBinding):

| application.yml property | Env var in docker-compose.yml |
|---|---|
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` |
| `app.jwt.secret` | `APP_JWT_SECRET` |
| `app.jwt.expiration-ms` | `APP_JWT_EXPIRATION_MS` |
| `app.upload.base-dir` | `APP_UPLOAD_BASE_DIR` |
| `app.cors.allowed-origins` | `APP_CORS_ALLOWED_ORIGINS` |
| `logging.level.com.rml` | `LOGGING_LEVEL_COM_RML` |

---

## Frontend / Nginx Details

```
Stage 1 (build):  node:20-alpine
  - npm ci         ← cached until package-lock.json changes
  - npm run build  ← outputs to dist/
  - ARG VITE_API_BASE_URL=/api  (kept as /api; Nginx handles proxy)

Stage 2 (runtime): nginx:1.27-alpine
  - Copies dist/ → /usr/share/nginx/html
  - Copies nginx.conf → /etc/nginx/conf.d/default.conf
  - EXPOSE: 80
```

**nginx.conf features:**
- `gzip` on for text/css/js/json/svg
- Static assets (`*.js`, `*.css`, etc.) served with `Cache-Control: public, immutable, max-age=1y` (safe because Vite hashes filenames)
- `/api/` location proxies to `http://backend:8080` — **no path rewrite** — the backend context-path `/api` is preserved as-is
- `/actuator/` location proxies to `/api/actuator/` on backend (public health probe endpoint)
- SPA fallback: `try_files $uri $uri/ /index.html` handles React Router client-side routes
- Security headers: X-Frame-Options, X-Content-Type-Options, X-XSS-Protection, Referrer-Policy
- `client_max_body_size 100m` matches the Spring Boot `max-file-size: 100MB` setting

---

## Docker Compose Service Summary

### `db` (mysql:8.0)
- Internal hostname: `db`
- healthcheck: `mysqladmin ping` — backend waits on `condition: service_healthy`
- Volume `db-data` persists MySQL data across container restarts
- `--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci`

### `backend` (system-backend:latest)
- Waits for `db` to be healthy before starting
- Flyway auto-applies V0-V3 migrations on startup
- Volume `uploads` bound to `/var/app/uploads`
- healthcheck: `wget -qO- http://localhost:8080/api/actuator/health | grep UP`
- `start_period: 90s` — Spring Boot + Flyway needs time on cold start

### `frontend` (system-frontend:latest)
- Waits for `backend` to be healthy
- Bound to host port `${HTTP_PORT:-80}:80`
- Only service exposed to the outside

---

## Environment Variables — Complete Reference

| Variable | Required | Default | Description |
|---|---|---|---|
| `MYSQL_ROOT_PASSWORD` | ✅ | — | MySQL root password (healthcheck + init) |
| `MYSQL_DATABASE` | | `system_db` | Database name |
| `MYSQL_USER` | | `appuser` | Application DB user |
| `MYSQL_PASSWORD` | ✅ | — | Application DB password |
| `JWT_SECRET` | ✅ | — | HS512 signing secret (≥ 32 chars; use `openssl rand -base64 64`) |
| `JWT_EXPIRATION_MS` | | `86400000` | Token lifetime in ms (24h) |
| `CORS_ALLOWED_ORIGINS` | | `http://localhost` | Comma-sep frontend origin(s) |
| `HTTP_PORT` | | `80` | Host port for Nginx |
| `JAVA_OPTS` | | `-Xmx512m -Xms256m` | JVM heap flags |
| `LOG_LEVEL` | | `INFO` | Log level for `com.rml.*` |

---

## Pre-Production Checklist

| Item | Status |
|---|---|
| Set `JWT_SECRET` to a cryptographically random value | ⚠️ REQUIRED |
| Set `MYSQL_ROOT_PASSWORD` and `MYSQL_PASSWORD` to strong passwords | ⚠️ REQUIRED |
| Set `CORS_ALLOWED_ORIGINS` to your production domain | ⚠️ REQUIRED |
| Remove `ports: "3306:3306"` from `db` service in docker-compose.yml | ⚠️ Recommended |
| Add TLS termination (SSL certificate) in front of Nginx | ⚠️ Recommended |
| Set `HTTP_PORT=443` and add Nginx HTTPS server block if TLS on Nginx | Optional |
| Confirm upload volume path has sufficient disk space | ⚠️ Required |
| Adjust `JAVA_OPTS` Xmx to ~75% of container memory limit | Recommended |

---

## Runtime Assumptions

1. **Docker Engine ≥ 24** with Docker Compose V2 (`docker compose` not `docker-compose`)
2. **BuildKit enabled** (default in Docker 24+) — required for multi-stage build layer caching
3. **MySQL 8.0** — Flyway V0-V3 tested against 8.0.45 in t30; `utf8mb4_unicode_ci` collation required
4. **Port 80** free on the host (or override with `HTTP_PORT`)
5. **Internet access during `docker compose build`** to pull base images and Maven/npm dependencies
6. **`/var/app/uploads` inside the backend container** is a named Docker volume — not a host path. If files need to be accessible outside Docker, change to a bind mount.
7. **Nginx `resolver 127.0.0.11`** — this is Docker's embedded DNS resolver. If deploying to non-Docker container runtimes (e.g., Podman, K8s), update the resolver accordingly.

---

## Known Non-Issues

| Item | Assessment |
|---|---|
| `nginx -t` fails outside Docker Compose | Expected — `backend` hostname only resolves inside Docker network. Config is correct. |
| `TINYINT(1)` deprecation warning in MySQL 8.0 | Non-error warning from V1 schema; no functional impact (confirmed in t30) |
| Backend CORS vs Nginx proxy | In production, frontend and backend share the same origin through Nginx, so CORS is effectively no-op. The `APP_CORS_ALLOWED_ORIGINS` is still set to avoid issues with direct API access. |
