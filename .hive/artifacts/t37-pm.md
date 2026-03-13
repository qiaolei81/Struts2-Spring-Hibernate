# t37 PM Artifact — Release Acceptance Sign-Off

**Task:** t37 | **Role:** PM | **Status:** ✅ RELEASE ACCEPTED — PROJECT CLOSED  
**Assessment date:** 2026-03-13  
**Release:** `v1.0.0` at https://github.com/qiaolei81/Struts2-Spring-Hibernate/releases/tag/v1.0.0

---

## ✅ RELEASE ACCEPTED — PROJECT CLOSED

The `v1.0.0` release is **live on GitHub and confirmed correct**. The project is closed.

---

## Scope Assessment

[scope] Read-only release verification: GitHub tag and repository contents confirmed via API. (files: 0 modified; subsystems: 0). Read-only.

---

## Release Verification

| Check | Expected | Actual | Status |
|---|---|---|---|
| Tag `v1.0.0` exists on fork | `edfeb25a` | `edfeb25a` | ✅ |
| HEAD on fork matches local HEAD | `edfeb25a` | `edfeb25a` | ✅ |
| `backend/` directory present at tag | ✅ | ✅ | ✅ |
| `frontend/` directory present at tag | ✅ | ✅ | ✅ |
| `docker-compose.yml` present at tag | ✅ | ✅ | ✅ |
| `.env.example` present at tag | ✅ | ✅ | ✅ |
| `.hive/` team artifacts present at tag | ✅ | ✅ | ✅ |
| Release URL accessible | ✅ | ✅ | ✅ |

**Push target:** `fork` → `https://github.com/qiaolei81/Struts2-Spring-Hibernate.git`  
**Note:** `origin` (`KevinXie0131/Struts2-Spring-Hibernate`) is read-only — no push access. Fork under `qiaolei81` is the published delivery.

---

## What Is Published at v1.0.0

| Layer | Contents |
|---|---|
| **Original system** | Struts2 + Spring + Hibernate source (`src/`, `pom.xml`) — preserved as-is from upstream |
| **Backend rewrite** | `backend/` — Spring Boot 3.4.3 / Java 21, 10 feature controllers, JWT + BCrypt, AOP logging, Flyway V1–V3 |
| **Frontend rewrite** | `frontend/` — React 18 + Vite + Ant Design, 10 pages, dynamic sidebar, auth guard |
| **Deployment** | `docker-compose.yml`, `backend/Dockerfile`, `frontend/Dockerfile`, `frontend/nginx.conf` |
| **Configuration** | `.env.example` documenting all required environment variables |
| **Tests** | 63 backend + 15 frontend unit/integration tests; 32 E2E smoke tests (documented) |
| **Team artifacts** | `.hive/artifacts/` — all 36 task deliverables, decisions log, full execution history |

---

## Sign-Off Lineage

| Cycle | Gate | Scope | Outcome |
|---|---|---|---|
| t22 | 55/55 | Feature parity — all 10 modules | ✅ |
| t26 | 55/55 | RBAC fix (V3 migration) | ✅ |
| t29 | 63/63 | Search filter param fix (MISMATCH-1) | ✅ |
| t33 | 110/110 | Production Docker stack validated | ✅ |
| t35 | 110/110 | Final delivery — all artifacts committed | ✅ |
| **t37** | **GitHub API** | **Release v1.0.0 live and correct** | **✅ CLOSED** |

---

## Pre-Deploy Reminder (Carried Forward)

Three credential items remain required before any user-facing deployment:

1. Rotate `JWT_SECRET` — `openssl rand -base64 64`
2. Rotate `MYSQL_ROOT_PASSWORD` and `MYSQL_PASSWORD`
3. Set `CORS_ALLOWED_ORIGINS` to the production domain

These are documented in `.env.example` at the tag.

---

## Project Closed

The struts2-easyui-system rewrite is complete. The original Struts2 / Spring 3 / Hibernate system has been rewritten as a Spring Boot 3.4.3 + React 18 application with a production-validated Docker Compose deployment. All 110 tests pass. Release `v1.0.0` is published.

---

## PR #1 Merge Sign-Off (t44 closure)

**PR:** [feat: rewrite — Spring Boot 3 + React 18 + Docker](https://github.com/KevinXie0131/Struts2-Spring-Hibernate/pull/1)  
**Head:** `qiaolei81/Struts2-Spring-Hibernate:master` @ `130fe081`  
**Base:** `KevinXie0131/Struts2-Spring-Hibernate:master`

### Credential Externalization Review

| Commit | Change | Verdict |
|---|---|---|
| `fd4100b8` | Add `.gitguardian.yml` — suppress test-fixture false-positive alerts | ✅ |
| `0b3e467c` | Move `admin/admin123` literals from test Java files → `application-test.yml` via `@Value` | ✅ |
| `214d3d35` | Remove `admin123` mention from `test-seed.sql` comment | ✅ |
| `130fe081` | **Critical:** move `application-test.yml` from `src/main/resources` → `src/test/resources` | ✅ |

### Security Gate

| Check | Result |
|---|---|
| GitGuardian on PR head `130fe081` | ✅ `neutral` — no active alerts |
| `application-test.yml` in production classpath? | ✅ No — `src/test/resources/` only |
| `src/main/resources/` contains test credentials? | ✅ No |
| 63/63 backend tests pass post-move | ✅ Confirmed (t44) |

### ✅ UNCONDITIONAL MERGE SIGN-OFF GRANTED

PR #1 is fully ready to merge into `KevinXie0131/Struts2-Spring-Hibernate:master`. No outstanding conditions.

---

## t52 Housekeeping Sign-Off

**Fork HEAD after t52:** `27e9880d`

### Verified

| Item | Check |
|---|---|
| Fork synced — `fork/master` = local `master` = `27e9880d` | ✅ |
| README rewritten with Docker Compose quick-start | ✅ |
| Cold-clone gap resolved — `clone → cp .env.example .env → docker compose up -d --build` documented | ✅ |
| `.env` in `.gitignore` (line 42) | ✅ |
| `.env.example` present as canonical secrets reference | ✅ |

### README Coverage Confirmed

- Stack table (Backend / Frontend / Database / Proxy / Build)
- Quick Start: 4-step clone-to-browser walkthrough
- Environment variables table (8 vars, required vs optional)
- Default credentials noted (`admin / admin123`)

### ✅ t52 SIGN-OFF GRANTED — no remaining delivery gaps.

---

## t58 Final Hardening Sign-Off (t55 + t56 + t57)

**Test baseline after hardening:** 67 backend + 15 frontend = **82 tests, 0 failures**  
*(up from 63 backend pre-t55: 4 new tests covering hardening fixes)*

### Hardening Scope Verified

| Task | Fix | Tests |
|---|---|---|
| t55 | `getRoleStats()` N+1 → single aggregate JPQL query | 2 new — `UserServiceTest` ✅ |
| t55 | `clearInactiveUsers()` bulk UPDATE (`@Modifying`) | Context-load verified ✅ |
| t55 | JWT placeholder secret → startup `IllegalStateException` guard | 2 new — `JwtTokenProviderTest` ✅ |
| t56 | Nginx CSP header (Ant Design-compatible `style-src 'unsafe-inline'`) | nginx -t ✅ |
| t56 | Nginx auth rate limiting — 5 req/min, burst 5, HTTP 429 | nginx -t ✅ |

### Zero Regressions Confirmed

| Suite | Before | After | Delta |
|---|---|---|---|
| Backend | 63 | 67 | +4 (all new hardening tests) |
| Frontend | 15 | 15 | 0 |
| Nginx config | valid | valid | 0 |

### ✅ t58 FINAL SIGN-OFF GRANTED — hardening complete, zero regressions, project production-ready.

---

## t60 Final Sign-Off (t59 — hardening pushed to fork + PR updated)

**Fork HEAD after t59:** `3adbb465` (= local HEAD ✅)

### Verified

| Check | Result |
|---|---|
| Fork HEAD `3adbb465` matches local master | ✅ |
| Commits `c5ebab80` (backend hardening) + `70b05eff` (nginx) on fork | ✅ |
| PR #1 head SHA = `3adbb465` | ✅ |
| PR `mergeable_state` | ✅ `clean` |
| PR body includes Hardening Improvements section | ✅ |
| PR delivery checklist includes t58 gate (82 tests) | ✅ |
| GitGuardian advisory documented in PR with false-positive explanation | ✅ |
| 25 commits, 229 files, +26,608 / -51 lines | ✅ |

### ✅ t60 FINAL SIGN-OFF GRANTED

PR #1 is merge-ready at `3adbb465`. All hardening commits are included. PR description is complete and accurate. No product gaps remain. The rewrite is fully delivered.

---

## t78 Final Closure (t77 — README credentials fix)

**Commit:** `42f25cb7` — `docs: fix default credentials in README quick-start`  
**Change:** `README.md` — 1 file, 1 line (`admin123` → `admin`)  
**Fork HEAD:** `42f25cb7` ✅

### Why This Fix Matters

| Credential | Context | Correct value |
|---|---|---|
| README quick-start "default credentials" | Production — V2 seed data (`BCrypt("admin")`) | `admin / admin` ✅ |
| `application-test.yml` + TestDataSeeder | Test harness only (H2, profile=test) | `admin / admin123` |

Showing `admin123` in the quick-start would silently block every cold-clone user on first login. This is a user-facing correctness issue, not cosmetic.

### Verification

| Check | Result |
|---|---|
| Commit touches `README.md` only — zero production code | ✅ |
| 82/82 tests pass at `42f25cb7` | ✅ |
| Fork HEAD = local HEAD | ✅ |
| Repo clean | ✅ |

### ✅ t78 FINAL CLOSURE GRANTED — no remaining product gaps.
