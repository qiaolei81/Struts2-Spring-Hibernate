# t30 DevOps Artifact — Staging MySQL Validation

**Task:** t30 | **Role:** devops | **Status:** ✅ ALL CLEAR — 30/30 smoke tests pass  
**Date:** 2026-03-13  
**MySQL version:** 8.0.45 (Docker `mysql:8.0`)  
**Application:** Spring Boot 3.4.3, Java 21, port 8099, context-path `/api`

---

## Scope Assessment

[scope] Staging MySQL validation: Docker MySQL + Flyway V0-V3 migrations + full endpoint smoke test (files: 1 artifact, subsystems: 1)

---

## Staging Environment

| Component | Detail |
|---|---|
| MySQL container | `mysql:8.0` via Docker, `t30-mysql-staging`, port `3307→3306` |
| Database | `system_db`, charset `utf8mb4`, collation `utf8mb4_unicode_ci` |
| Application JAR | `backend/target/system-backend-1.0.0-SNAPSHOT.jar` (77 MB) |
| App port | `8099` (to avoid conflict with other services) |
| Upload dir | `/tmp/t30-staging/uploads` |
| Docker Compose | `/tmp/t30-staging/docker-compose.yml` (transient, not committed) |

---

## Flyway Migration Results

All 4 migrations applied cleanly on a **fresh empty schema** — no baseline conflicts, no checksum errors.

| installed_rank | version | description | script | checksum | success |
|---|---|---|---|---|---|
| 1 | 0 | baseline | V0__baseline.sql | -1722623445 | ✅ 1 |
| 2 | 1 | schema | V1__schema.sql | 165024081 | ✅ 1 |
| 3 | 2 | seed | V2__seed.sql | -1499625244 | ✅ 1 |
| 4 | 3 | fix role name | V3__fix_role_name.sql | 1706805489 | ✅ 1 |

**"Successfully applied 4 migrations to schema `system_db`, now at version v3 (execution time 00:00.368s)"**

> One non-blocking warning from MySQL 8.0: `TINYINT(1)` display-width deprecation notice. Not an error; schema is valid.

### Tables Created by V1

All 9 expected tables present:

| TABLE_NAME | Seed Rows |
|---|---|
| t_user | 1 (admin) |
| t_role | 3 (ADMIN, Guest, User) |
| t_authority | 34 |
| t_menu | 13 |
| t_user_role | 1 |
| t_role_authority | 34 |
| t_equipment | 0 (seeded by smoke test, cleaned up) |
| t_document | 0 (seeded by smoke test) |
| t_access_log | 4 (written by AOP during test) |

---

## V3 Migration Verification — RBAC Role Name Fix

```sql
SELECT id, name, description FROM t_role ORDER BY id;
+----+-------+--------------------------------------+
| id | name  | description                          |
+----+-------+--------------------------------------+
| 0  | ADMIN | Full access to all system functions  |
| 1  | Guest | Minimum read-only access             |
| 2  | User  | Standard operator access             |
+----+-------+--------------------------------------+
```

✅ **V3 applied correctly**: `t_role.name` for id='0' is `'ADMIN'` (was `'Administrator'` in V2).  
✅ The RBAC chain `'ADMIN'` → `ROLE_ADMIN` → `hasRole('ADMIN')` is **sound in production MySQL**.

### Full RBAC Chain (admin user)

```
t_user.username = 'admin' (id='0')
  → t_user_role → t_role.name = 'ADMIN'
  → UserDetailsServiceImpl: GrantedAuthority = "ROLE_ADMIN"
  → @PreAuthorize("hasRole('ADMIN')") → GRANTED ✅

Plus 27 permission authorities (PERM_*) via t_role_authority:
  PERM_AUTH_{ADD,DELETE,EDIT,LIST}
  PERM_DOC_{ADD,DELETE,EDIT,LIST,UPLOAD}
  PERM_EQUIP_{ADD,DELETE,EDIT,LIST}
  PERM_LOG_LIST
  PERM_MENU_{ADD,DELETE,EDIT,LIST}
  PERM_ROLE_{ADD,DELETE,EDIT,LIST}
  PERM_USER_{ADD,DELETE,EDIT,LIST,ROLE_EDIT}
```

---

## Smoke Test Results — 30/30 PASS

Executed against live `MySQL 8.0.45` staging instance with Flyway V0-V3 applied.

### Security Boundary Tests

| Test | Expected | Result |
|---|---|---|
| GET /health (no auth) | 401 | ✅ 401 |
| GET /health (admin JWT) | 200 | ✅ 200 |
| GET /actuator/health (public) | 200 | ✅ 200 |
| GET /users (no auth) | 401 | ✅ 401 |

### Auth Endpoints

| Test | Expected | Result |
|---|---|---|
| POST /auth/login (correct creds) | 200 | ✅ 200 |
| POST /auth/login (wrong creds) | 401 | ✅ 401 |
| POST /auth/logout | 200 | ✅ 200 |

### RBAC-Protected Endpoints (all require ADMIN or matching PERM_*)

| Test | Expected | Result |
|---|---|---|
| GET /users | 200 | ✅ 200 |
| GET /users/all | 200 | ✅ 200 |
| GET /users/stats/by-role | 200 | ✅ 200 |
| GET /roles | 200 | ✅ 200 |
| GET /roles/all | 200 | ✅ 200 |
| GET /authorities | 200 | ✅ 200 |
| GET /authorities/tree | 200 | ✅ 200 |
| GET /menus | 200 | ✅ 200 |
| GET /menus/tree | 200 | ✅ 200 |
| GET /equipment | 200 | ✅ 200 |
| GET /equipment/export (Excel) | 200 | ✅ 200 |
| GET /documents | 200 | ✅ 200 |
| GET /logs | 200 | ✅ 200 |
| GET /online | 200 | ✅ 200 |
| GET /online-users | 200 | ✅ 200 |
| GET /users/stats/by-role (chart) | 200 | ✅ 200 |

### Search Filter Tests (MISMATCH-1 fix — t27/t28)

| Test | Expected | Result |
|---|---|---|
| GET /users?name=admin (partial match) | non-empty | ✅ content has results |
| GET /users?name=zzz-no-match | empty | ✅ content=[] |
| GET /roles?name=ADMIN (partial match) | non-empty | ✅ content has results |

### CRUD Round-Trip Tests

| Test | Expected | Result |
|---|---|---|
| POST /equipment (create) | 201 | ✅ 201 |
| PUT /equipment/{id} (update) | 200 | ✅ 200 |
| DELETE /equipment?ids={id} | 200 | ✅ 200 |
| POST /documents (create) | 201 | ✅ 201 |

### AOP Access Log Verification

AOP wrote 4 entries during the test run (confirmed in `t_access_log`):

| username | message |
|---|---|
| admin | Login success |
| admin | Login success |
| admin | Login failed: Invalid username or password |
| admin | Login success |

✅ AOP logging is active and writing to `t_access_log` in MySQL.

---

## Runtime Assumptions Validated

| Assumption | Verified |
|---|---|
| MySQL 8.0 InnoDB with utf8mb4 | ✅ |
| Flyway applies V0→V3 on empty schema | ✅ |
| BCrypt password hash `$2b$10$...` works with Spring Security | ✅ |
| JWT HS512 auth token issued and verified | ✅ |
| HikariCP pool starts (min-idle=5, max=20) | ✅ |
| `spring.jpa.hibernate.ddl-auto: validate` passes against Flyway schema | ✅ |
| AOP `@AfterReturning` / `@AfterThrowing` writes to `t_access_log` | ✅ |
| Both `/online` and `/online-users` path aliases work | ✅ |
| Excel export endpoint returns valid binary | ✅ |

---

## Known Non-Issues

| Item | Assessment |
|---|---|
| `TINYINT(1)` display-width deprecation warning | MySQL 8.0 warning only — not an error; schema is valid |
| `MySQLDialect` explicit config warning | Hibernate recommends auto-detect; no functional impact |
| `/api/health` returns 401 without JWT | By design: `SecurityConfig` does not permit `/health`; use `/actuator/health` for liveness probes |

---

## Operational Prerequisites for Production Deploy

All staging gates from t26 PM sign-off are now **confirmed satisfied**:

1. ✅ V3 migration applies cleanly against real MySQL 8.0  
2. ✅ `SELECT name FROM t_role WHERE id='0'` returns `'ADMIN'`  
3. ✅ Login as admin + `GET /users` returns 200 (not 403)  
4. ✅ All 30 smoke tests pass against real MySQL (not H2)

### Pre-Production Checklist

| Item | Status |
|---|---|
| Change `app.jwt.secret` from default placeholder | ⚠️ REQUIRED before go-live |
| Change `spring.datasource.password` from `changeme` | ⚠️ REQUIRED before go-live |
| Configure `app.upload.base-dir` to production path | ⚠️ REQUIRED |
| Configure `app.cors.allowed-origins` to production domain | ⚠️ REQUIRED |
| Run V0→V3 migrations against production DB (copy) first | ⚠️ Recommended dry run |
| Verify upload directory exists and is writable | ⚠️ REQUIRED |
| Use `/actuator/health` for liveness/readiness probes (not `/health`) | ✅ Confirmed public |

---

## Summary

The staging MySQL validation is **complete and fully green**.

- **Flyway V0-V3**: all 4 migrations applied cleanly to a fresh MySQL 8.0.45 database in 368ms
- **V3 RBAC fix**: confirmed in production MySQL — `t_role.name='ADMIN'` for id='0'
- **All 30 smoke tests pass**: security boundaries, auth, RBAC, all 10 controllers, CRUD, search filter, AOP logging
- **No blockers for production deploy** beyond standard credential rotation

The only remaining operational prerequisites are credential/config changes (JWT secret, DB password, CORS origins, upload path) — not code defects.
