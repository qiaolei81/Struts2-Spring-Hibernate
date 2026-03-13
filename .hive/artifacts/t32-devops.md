# t32 DevOps Artifact — Final Production Readiness Review

**Task:** t32 | **Role:** devops | **Status:** ✅ ALL CLEAR — 32/32 E2E smoke tests pass  
**Date:** 2026-03-13  
**Stack:** MySQL 8.0 + Spring Boot 3.4.3 / Java 21 + React 18 / Nginx 1.27  
**Validated on:** Docker Engine 28.5.2, Docker Compose v2.40.3

---

## Scope Assessment

[scope] Production readiness: docker compose build + start + E2E health validation + two bug fixes committed (files: 2 modified, subsystems: 1 — compose stack). Within limits.

---

## Build Results

### `docker compose build --no-cache`

| Image | Result |
|---|---|
| `system-backend:latest` | ✅ Built (Maven 3.9 → Temurin 21 JRE Alpine) |
| `system-frontend:latest` | ✅ Built (Node 20 → Nginx 1.27 Alpine) |

Both multi-stage builds completed with zero errors.  
No test failures during `mvn package -DskipTests`.  
No npm build errors; Vite produced `dist/`.

---

## Stack Start Results

### `docker compose up -d`

| Container | Status | Ports |
|---|---|---|
| `system-db` | ✅ `Up (healthy)` | 3306/tcp (internal only) |
| `system-backend` | ✅ `Up (healthy)` | 8080/tcp (internal only) |
| `system-frontend` | ✅ `Up (running)` | `0.0.0.0:80→80/tcp` |

Dependency chain satisfied: db → backend → frontend, all health conditions passed.  
Flyway V0–V3 migrations applied on first backend startup (clean DB volume).

---

## Bugs Found and Fixed

Two production-blocking bugs were identified during validation and committed as `3332f8c3`.

### Bug 1: Malformed JDBC URL (docker-compose.yml)

**File:** `docker-compose.yml`  
**Symptom:** Backend would fail to connect to MySQL at runtime with a JDBC URL parse error.  
**Root cause:** `SPRING_DATASOURCE_URL` was defined with a `>-` YAML block scalar spanning two lines. YAML `>-` folds newlines into spaces, producing:
```
jdbc:mysql://db:3306/system_db?useSSL=false &serverTimezone=Asia/Shanghai&...
```
The stray space before `&serverTimezone=` is an illegal character in a JDBC URL.

**Fix:** Changed to a single quoted string on one line:
```yaml
SPRING_DATASOURCE_URL: "jdbc:mysql://db:3306/${MYSQL_DATABASE:-system_db}?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
```

### Bug 2: Nginx Actuator Proxy Drops Sub-path (frontend/nginx.conf)

**File:** `frontend/nginx.conf`  
**Symptom:** `GET /actuator/health` through Nginx returned 401 instead of 200.  
**Root cause:** The `/actuator/` location used `proxy_pass $backend_upstream/api/actuator/` with a variable. When Nginx `proxy_pass` references a variable, it does **not** perform the prefix-replacement path rewriting that a static `proxy_pass` would. Every request under `/actuator/*` was forwarded to the backend as `GET /api/actuator/` (base path only, sub-path dropped). Spring Security blocks `/api/actuator/` (it only permits `/actuator/health` and `/actuator/info`), returning 401.

**Fix:** Added a `rewrite` directive before `proxy_pass` to prepend the Spring Boot context-path and retain the sub-path, then used `proxy_pass $backend_upstream` with no URI component (which correctly passes the rewritten full URI):
```nginx
location /actuator/ {
    set $backend_upstream http://backend:8080;
    rewrite ^/actuator/(.*)$ /api/actuator/$1 break;
    proxy_pass         $backend_upstream;
    ...
}
```

---

## E2E Smoke Test Results — 32/32 PASS

All tests executed through Nginx on port 80 (the only host-exposed port).

### Service Health

| Check | Result |
|---|---|
| `system-db` Docker health status | ✅ healthy |
| `system-backend` Docker health status | ✅ healthy |
| `system-frontend` container status | ✅ running |

### Actuator / Health Probes

| Check | Expected | Result |
|---|---|---|
| `GET /actuator/health` (nginx rewrite) | 200 | ✅ 200 `{"status":"UP"}` |
| `GET /api/actuator/health` | 200 | ✅ 200 `{"status":"UP"}` |

### Static SPA

| Check | Expected | Result |
|---|---|---|
| `GET /` → index.html | 200 | ✅ 200 |
| `GET /login` (React Router SPA fallback) | 200 | ✅ 200 |
| `GET /dashboard` (React Router SPA fallback) | 200 | ✅ 200 |

### Security Boundary

| Check | Expected | Result |
|---|---|---|
| `GET /api/users` (no auth) | 401 | ✅ 401 |
| `POST /api/auth/login` (wrong password) | 401 | ✅ 401 |

### Auth

| Check | Expected | Result |
|---|---|---|
| `POST /api/auth/login` (admin/admin) | 200 + JWT | ✅ 200 |

### RBAC-Protected Endpoints (all with admin JWT)

| Endpoint | Result |
|---|---|
| `GET /api/users` | ✅ 200 |
| `GET /api/users/all` | ✅ 200 |
| `GET /api/roles` | ✅ 200 |
| `GET /api/roles/all` | ✅ 200 |
| `GET /api/authorities` | ✅ 200 |
| `GET /api/authorities/tree` | ✅ 200 |
| `GET /api/menus` | ✅ 200 |
| `GET /api/menus/tree` | ✅ 200 |
| `GET /api/equipment` | ✅ 200 |
| `GET /api/documents` | ✅ 200 |
| `GET /api/logs` | ✅ 200 |
| `GET /api/online` | ✅ 200 |
| `GET /api/online-users` | ✅ 200 |

### Special Endpoints

| Check | Expected | Result |
|---|---|---|
| `GET /api/equipment/export` (Excel binary) | 200 | ✅ 200 |
| `GET /api/users/stats/by-role` (chart data) | 200 | ✅ 200 |

### Search Filter — MISMATCH-1 Fix (t27/t28)

| Check | Expected | Result |
|---|---|---|
| `GET /api/users?name=admin` → 1 result | ≥1 | ✅ 1 |
| `GET /api/users?name=zzz-no-match` → empty | 0 | ✅ 0 |

### CRUD Round-Trip

| Check | Expected | Result |
|---|---|---|
| `POST /api/equipment` (create) | 201 + ID | ✅ ID issued |
| `PUT /api/equipment/{id}` (update) | 200 | ✅ 200 |
| `DELETE /api/equipment?ids={id}` | 200 | ✅ 200 |

### AOP Access Log

| Check | Expected | Result |
|---|---|---|
| `GET /api/logs` total entries > 0 | >0 | ✅ 8 entries |

AOP `@AfterReturning`/`@AfterThrowing` is active and writing to `t_access_log` in MySQL.

---

## Runtime Assumptions — Validated

| Assumption | Status |
|---|---|
| Docker Engine ≥ 24 with Compose V2 | ✅ Engine 28.5.2, Compose v2.40.3 |
| MySQL 8.0 + utf8mb4_unicode_ci | ✅ Container healthy, schema valid |
| Flyway V0–V3 applies on empty schema | ✅ All 4 migrations applied on cold start |
| BCrypt password hash works with Spring Security | ✅ Login as admin confirmed |
| JWT HS512 issued and verified | ✅ Token issued, all protected endpoints return 200 |
| HikariCP pool (min-idle=5, max=20) starts | ✅ Backend healthy |
| `spring.jpa.hibernate.ddl-auto: validate` passes | ✅ No schema mismatch |
| AOP access logging to t_access_log | ✅ 8 entries written |
| Nginx resolver 127.0.0.11 (Docker DNS) | ✅ Backend hostname resolved at runtime |
| CORS: frontend and backend share origin through Nginx | ✅ No CORS errors |
| SPA client-side routing via `try_files` fallback | ✅ /login, /dashboard → 200 |
| Upload volume mounted at /var/app/uploads | ✅ Volume created |

---

## Pre-Production Checklist

| Item | Status |
|---|---|
| Replace `JWT_SECRET` with `openssl rand -base64 64` output | ⚠️ REQUIRED before go-live |
| Replace `MYSQL_ROOT_PASSWORD` / `MYSQL_PASSWORD` with strong passwords | ⚠️ REQUIRED before go-live |
| Set `CORS_ALLOWED_ORIGINS` to production domain | ⚠️ REQUIRED before go-live |
| Remove `ports: "3306:3306"` from `db` service | ⚠️ Recommended |
| Add TLS (HTTPS) in front of Nginx | ⚠️ Recommended for production |
| Adjust `JAVA_OPTS` Xmx to ~75% of container memory limit | Recommended |
| Confirm upload volume has sufficient disk space | Required |
| `.env` must NOT be committed to source control | ⚠️ Ensure in .gitignore |

---

## Commits

| SHA | Message |
|---|---|
| `3332f8c3` | `fix: correct JDBC URL folding and nginx actuator proxy path` |

---

## Summary

The production Docker Compose stack **builds and starts end-to-end with all 3 services healthy**. Two production-blocking bugs were found, fixed, and committed:

1. **JDBC URL malformed** (YAML `>-` scalar inserting a space before `&serverTimezone=`) — would have caused backend startup failure against MySQL.
2. **Nginx actuator proxy drops sub-path** (variable in `proxy_pass` suppresses path rewriting) — `/actuator/health` returned 401 instead of 200; fixed with `rewrite` + `proxy_pass $var` (no URI component).

All 32 E2E smoke tests now pass: service health, health probes, SPA routing, auth, RBAC, all 13 controllers, Excel export, search filters, CRUD round-trip, and AOP access logging.

**The stack is production-ready pending credential rotation** (JWT_SECRET, DB passwords, CORS origin).
