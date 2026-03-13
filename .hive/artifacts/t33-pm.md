# t33 PM Artifact — Final Production Acceptance Sign-Off

**Task:** t33 | **Role:** PM | **Status:** ✅ SIGN-OFF GRANTED — WITH DEPLOYMENT GATE  
**Assessment date:** 2026-03-13  
**Prior sign-offs:** t22 (feature parity), t26 (RBAC fix), t29 (search filter fix), t32 (infra E2E validation)  
**This scope:** Final acceptance — rewrite completeness, production deployment artifacts, git state audit

---

## ✅ SIGN-OFF GRANTED — REWRITE COMPLETE

The rewrite is **fully accepted as a product**. All 10 feature modules are implemented, all acceptance criteria from the t2 inventory are met, and 110/110 tests pass (63 backend + 15 frontend + 32 E2E smoke). The production Docker stack builds clean and starts all three services healthy.

**One deployment gate remains open** (not a product defect — a git state issue): the production deployment artifacts (Dockerfiles, `.env.example`, `.dockerignore` files) exist on disk and have been validated, but are **not committed to the repository**. These must be committed before a clean-repo deployment is possible. This does not affect product acceptance; it affects deployment repeatability.

---

## Scope Assessment

[scope] Read-only final acceptance audit: all production artifacts reviewed end-to-end including git state, test results, deployment config, and E2E validation. (files: 0 modified; subsystems: 2 — docker stack + application code). Read-only — exempt from escalation.

---

## ⚠️ OPEN DEPLOYMENT GATE: Untracked Production Artifacts

The following files exist on disk (confirmed working by t32) but are **NOT committed to git**:

| File | Description | State |
|---|---|---|
| `backend/Dockerfile` | Multi-stage Maven + Temurin 21 JRE build | ⚠️ Untracked |
| `backend/.dockerignore` | Excludes `target/` and local config from Docker build context | ⚠️ Untracked |
| `frontend/Dockerfile` | Multi-stage Node 20 Vite build + Nginx 1.27 serve | ⚠️ Untracked |
| `frontend/.dockerignore` | Excludes `node_modules/`, `dist/` from Docker build context | ⚠️ Untracked |
| `.env.example` | Operator documentation for all required environment variables | ⚠️ Untracked |

Additionally, two test files have improvements that exist in the working tree but were not committed with the final t28 commit:

| File | Uncommitted Change |
|---|---|
| `backend/src/test/.../FeatureApiContractIntegrationTest.java` | Stronger assertions (content value checks, not just `.isArray()`) and display name improvements from t28 |
| `backend/src/test/resources/test-seed.sql` | Search filter seed data (2 equipment + 2 document records) required by filter proof tests |

**Impact:** A fresh `git clone` of the repository would be missing the Dockerfiles and `.env.example`. `docker compose up --build` would fail. The test improvements are also lost on a fresh clone, reducing test signal quality.

**Action required before any production deployment:** Commit all untracked deployment artifacts and the working-tree test file changes. This is an operational step, not a code change.

[notify:devops] Untracked deployment artifacts found in git working tree: `backend/Dockerfile`, `frontend/Dockerfile`, `backend/.dockerignore`, `frontend/.dockerignore`, `.env.example` are all present on disk but NOT committed. Also `backend/src/test/.../FeatureApiContractIntegrationTest.java` and `backend/src/test/resources/test-seed.sql` have uncommitted working-tree changes. These must be committed before a clean deployment is possible. Please commit in a follow-up task.

---

## Test Results Summary

| Suite | Count | Pass | Fail | Source |
|---|---|---|---|---|
| Backend unit + integration | 63 | 63 | 0 | `mvn test -Dspring.profiles.active=test` (run before this sign-off) |
| Frontend unit | 15 | 15 | 0 | `npm test` (vitest) |
| E2E smoke through Nginx :80 | 32 | 32 | 0 | t32 devops — `docker compose up` + HTTP probes |
| **Total** | **110** | **110** | **0** | |

---

## Production Bugs Fixed (Commit 3332f8c3)

Two bugs would have caused production failure. Both are now fixed and committed.

### Bug 1: Malformed JDBC URL — `docker-compose.yml`

| | |
|---|---|
| **Symptom** | Backend would fail to connect to MySQL at container startup with a JDBC URL parse error |
| **Root cause** | `SPRING_DATASOURCE_URL` defined with YAML `>-` block scalar spanning two lines; `>-` folds newlines into spaces, producing `...useSSL=false &serverTimezone=...` (illegal space before `&`) |
| **Fix** | Changed to single-quoted string on one line — no newline folding |
| **Current value** | `"jdbc:mysql://db:3306/${MYSQL_DATABASE:-system_db}?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"` |
| **Verified** | ✅ `system-backend` container status: `Up (healthy)` — DB connection established |

### Bug 2: Nginx Actuator Proxy Drops Sub-path — `frontend/nginx.conf`

| | |
|---|---|
| **Symptom** | `GET /actuator/health` through Nginx returned 401 instead of 200 — liveness probes fail |
| **Root cause** | `proxy_pass $backend_upstream/api/actuator/` with a variable: when `proxy_pass` references a variable, Nginx does NOT perform prefix-replacement path rewriting; every `/actuator/health` request arrived at the backend as `GET /api/actuator/` (sub-path dropped), which Spring Security blocks |
| **Fix** | `rewrite ^/actuator/(.*)$ /api/actuator/$1 break;` before `proxy_pass $backend_upstream;` (no URI component — variable proxy_pass then passes the rewritten URI correctly) |
| **Verified** | ✅ `GET /actuator/health` → 200 `{"status":"UP"}` through Nginx |

---

## E2E Smoke Test Coverage — 32/32 Pass

All tests ran through Nginx on port 80 (the only host-exposed port).

| Category | Tests | Pass |
|---|---|---|
| Docker service health (db, backend, frontend) | 3 | ✅ 3 |
| Actuator health probes (via Nginx rewrite + direct) | 2 | ✅ 2 |
| SPA static serving + client-side routing fallback | 3 | ✅ 3 |
| Security boundary (unauthenticated → 401) | 2 | ✅ 2 |
| Authentication (login → JWT) | 1 | ✅ 1 |
| RBAC-protected endpoints (13 controllers) | 13 | ✅ 13 |
| Special endpoints (Excel export, stats chart) | 2 | ✅ 2 |
| Search filter — MISMATCH-1 fix (match + no-match) | 2 | ✅ 2 |
| CRUD round-trip (create → update → delete) | 3 | ✅ 3 |
| AOP access log entries written to MySQL | 1 | ✅ 1 |
| **Total** | **32** | **✅ 32** |

---

## Cumulative Sign-Off Record

All conditions from every prior acceptance cycle are confirmed still green.

| Cycle | Condition | Status |
|---|---|---|
| t22 | 10 modules with working REST endpoints and frontend pages | ✅ |
| t22 | 22/22 contract tests pass | ✅ (now 30/30, expanded) |
| t22 | 12/12 PM verification scenarios pass | ✅ |
| t22 | E2E login → JWT → protected request proven | ✅ (smoke-tested live on port 80) |
| t22 | File upload max size: 100 MB | ✅ |
| t26 | V3 migration: RBAC role name fix (Administrator → ADMIN) | ✅ (applied by Flyway on cold start) |
| t26 | Production RBAC chain: ROLE_ADMIN resolves correctly | ✅ (admin login + all 13 endpoints → 200) |
| t29 | MISMATCH-1: search `?name=` param binding in all 4 list controllers | ✅ (smoke-tested: `?name=admin` → 1 result) |
| t33 | Stack builds clean (3-service docker compose) | ✅ |
| t33 | JDBC URL correct (no YAML folding artifact) | ✅ |
| t33 | Nginx actuator proxy sub-path preserved | ✅ |
| t33 | Flyway V0–V3 applies on empty DB volume | ✅ |
| t33 | AOP logging writes to MySQL (not just H2) | ✅ (8 entries confirmed) |
| t33 | CORS: no errors through Nginx reverse proxy | ✅ |
| t33 | SPA client-side routing via `try_files` fallback | ✅ |

---

## Pre-Production Requirements (Not Blocking Sign-Off)

These items are operational, not code defects. Sign-off is granted now; they must be completed before the first user-facing deployment.

| Item | Priority | Action |
|---|---|---|
| Replace `JWT_SECRET` | 🔴 REQUIRED | `openssl rand -base64 64` → `.env` |
| Replace `MYSQL_ROOT_PASSWORD` and `MYSQL_PASSWORD` | 🔴 REQUIRED | Strong passwords → `.env` |
| Set `CORS_ALLOWED_ORIGINS` to production domain | 🔴 REQUIRED | e.g. `https://system.example.com` |
| Remove or block `ports: "3306:3306"` on `db` service | 🟡 Recommended | Prevent direct external DB access |
| Add TLS/HTTPS termination in front of Nginx | 🟡 Recommended | Reverse proxy or cloud LB |
| Confirm `.env` is in `.gitignore` | 🟡 Required | Secrets must not be committed |
| Set container memory limits and tune `JAVA_OPTS -Xmx` | 🟢 Recommended | ~75% of container memory limit |
| Verify upload volume disk capacity | 🟢 Required | Must hold user-uploaded manual files |

---

## What Is Accepted

The following is accepted as complete and production-ready:

| Layer | Deliverable | Status |
|---|---|---|
| Database | MySQL schema (V1: 9 tables), seed (V2: admin + roles + menus), RBAC fix (V3) | ✅ |
| Backend | Spring Boot 3.4.3 / Java 21: 10 feature controllers + services + AOP + JWT + BCrypt | ✅ |
| Frontend | React 18 + Vite + Ant Design: 10 pages + dynamic sidebar + auth guard | ✅ |
| Infrastructure | Docker Compose: db (MySQL 8) + backend (Temurin 21 JRE) + frontend (Nginx 1.27) | ✅ |
| Code parity | All 10 original Struts2/EasyUI modules faithfully reproduced under accepted ADRs | ✅ |

## What Is Not in Scope (Accepted Omissions)

| Item | Disposition |
|---|---|
| TLS/HTTPS | Operational — handled outside this stack |
| Credential rotation | Operational — required before go-live |
| Horizontal scaling | Out of scope for MVP |
| Database backup policy | Operational — outside this codebase |

---

## Cycle History

| Cycle | Gate | Finding | Outcome |
|---|---|---|---|
| t14–t19 | 0/22 contract | Backend absent (3 cycles) | BLOCKED |
| t22 | 55/55 | Full feature parity | ✅ ACCEPTED |
| t26 | 55/55 | V3 RBAC fix sound | ✅ ACCEPTED (staging gate) |
| t29 | 63/63 | MISMATCH-1 search filter fixed | ✅ ACCEPTED |
| **t33** | **110/110** | Infra bugs fixed, stack validated E2E on port 80; untracked deployment artifacts found and flagged | **✅ PRODUCT ACCEPTED — deployment gate open** |
