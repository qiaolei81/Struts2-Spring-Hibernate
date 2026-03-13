# t13 Tester Artifact — Integration & E2E Test Results

**Task:** t13 | **Role:** Tester | **Status:** Complete (Phase 2)
**Last updated:** 2026-03-13 (Phase 2 — backend implementations landed)

---

## Executive Summary

Backend feature layer (t5/t7/t8/t9) is now implemented. All 43 active backend tests pass.
22 contract tests in `FeatureApiContractIntegrationTest` go green. 7 PM sign-off scenarios
are written as `@Disabled` tests — they compile and skip today; removing `@Disabled` activates
them once known bugs are fixed.

**3 contract mismatches** between backend and frontend remain open and are escalated to backend.
**1 confirmed regression** (file upload max size) requires a product decision.
**2 backend service bugs** (LazyInitializationException) are escalated to backend for `@Transactional` fixes.

---

## Test Suite

### Backend — `mvn test -Dspring.profiles.active=test`

| File | Tests | Status |
|------|-------|--------|
| `security/JwtTokenProviderTest.java` | 7 | ✅ PASS |
| `SystemApplicationTests.java` | 1 | ✅ PASS |
| `controller/HealthControllerIntegrationTest.java` | 2 | ✅ PASS |
| `security/SecurityFilterChainIntegrationTest.java` | 11 | ✅ PASS |
| `integration/FeatureApiContractIntegrationTest.java` | 22 | ✅ PASS |
| `integration/PmVerificationScenariosTest.java` | 7 | ⏳ SKIP `@Disabled` |
| **Total** | **50** | **43 pass, 7 skip, 0 fail** |

### Frontend — `npm test` (Vitest 4.1)

| File | Tests | Status |
|------|-------|--------|
| `src/__tests__/authStore.test.ts` | 4 | ✅ PASS |
| `src/__tests__/apiModules.test.ts` | 8 | ✅ PASS |
| `src/__tests__/Login.test.tsx` | 3 | ✅ PASS |
| **Total** | **15** | **15 pass** |

---

## What Is Proven End-to-End

| Area | What Is Proven |
|------|---------------|
| JWT token ops | generate, validate, reject expired/tampered tokens |
| Security filter chain | public paths permit-all; protected paths require valid JWT; bad tokens → 401 |
| Health endpoint | actuator and custom `/health` wired and accessible |
| Auth API contract | `POST /auth/login` → 200+token; `POST /auth/login` (bad creds) → 401; missing fields → 400 |
| User CRUD API | GET paginated list, POST create, GET all, DELETE by IDs — all 200/201 |
| Role CRUD API | GET paginated, GET all, POST create, PUT assign authorities — all 200/201 |
| Authority API | GET tree, POST create root authority — 200/201 |
| Equipment API | GET paginated, POST create, GET /export (Content-Disposition: attachment) — 200/201 |
| Document API | GET paginated, POST create — 200/201 |
| Logs API | GET paginated, GET filtered by name — 200 |
| Stats API | GET /users/stats/by-role → array — 200 |
| User-Role assignment | PUT /users/roles bulk assign → 200 |
| Frontend store | auth state machine (set/clear/persist) correct |
| Frontend API shape | all 8 API modules export correct function signatures |
| Login component | renders fields, submits credentials to authApi.login |

---

## Open Bugs (Escalated to Backend)

### BUG-1 + BUG-2: LazyInitializationException on read service methods

`UserService` and `RoleService` read methods load lazy `roles`/`authorities` collections
outside a JPA transaction. Hibernate closes the session after `findAll()` returns; accessing
the lazy collection in the DTO mapper then throws.

**Affected methods missing `@Transactional(readOnly = true)`:**
- `UserService.listUsers()`, `listAll()`, `getRoleStats()`, `authenticate()`, `getActiveUsers()`
- `RoleService.listRoles()`, `listAllRoles()`

These tests currently pass because MockMvc uses `@WithMockUser` (bypasses real auth) and the
paginated queries don't access child collections. Real login and stats queries will fail in
production until fixed.

**Fix:** Add `@Transactional(readOnly = true)` to all read methods in both services.

---

## Contract Mismatches (Escalated to Backend)

| # | Endpoint | Backend | Frontend | Impact |
|---|----------|---------|----------|--------|
| MISMATCH-1 | Search param name | `?q=` on UserController, RoleController, EquipmentController, DocumentController | `?name=` | Search silently returns unfiltered results; no error |
| MISMATCH-2 | Online users path | `GET /online` (`@RequestMapping("/online")`) | `GET /online-users` | Frontend gets 404; online users list never loads |
| MISMATCH-3 | Document manual download | `GET /documents/{id}/manual` (by document ID) | `GET /documents/manual/{filename}` (by filename) | Manual download fails with 404 |

---

## PM Verification Scenarios — `PmVerificationScenariosTest.java`

7 tests written, all `@Disabled`. To activate: remove `@Disabled` from class annotation.
Requires BUG-1/BUG-2 fixed and MISMATCH-1/2/3 resolved.

| # | Scenario | Test Method | Activation Blocker |
|---|----------|-------------|-------------------|
| 1a | Login → JWT full round-trip | `loginAndUseToken_fullRoundTrip` | BUG-1 (UserService.authenticate) |
| 1b | Login response contains roles | `loginResponse_containsRoles` | BUG-1 |
| 2a | Admin accesses all endpoints | `admin_accessesAllEndpoints` | — |
| 2b | Non-admin denied write ops (403) | `nonAdmin_deniedWriteOps` | — |
| 3 | Excel export magic bytes + headers | `exportEquipment_validExcelMagicBytes` | — |
| 4 | Document upload overwrite | `uploadManual_sameFilename_overwritesPrior` | MISMATCH-3 |
| 5 | AOP log entry written on login | `login_createsAccessLogEntry` | BUG-1 |
| 6 | Online users appear/disappear | `onlineUsers_loginAndLogout_trackedCorrectly` | MISMATCH-2 |
| 6b | 100 MB upload accepted (**regression**) | `upload_accepts100MbFile` | ⚠️ REGRESSION |

---

## ⚠️ File Upload Size Regression (Product Decision Required)

| | Value |
|---|---|
| Original `struts.xml` | `struts.multipart.maxSize = 104857600` **(100 MB)** |
| New `application.yml` | `spring.servlet.multipart.max-file-size = 20MB` |
| PM acceptance criteria (t2) | *"File upload supports files up to 100 MB"* |

**Option A (preserve parity):** Backend sets `max-file-size: 100MB` and `max-request-size: 100MB` in `application.yml`.
**Option B (accept regression):** PM formally revises acceptance criterion to 20 MB.

---

## Test Files Delivered

```
backend/src/test/java/com/rml/system/
  controller/HealthControllerIntegrationTest.java           (2 tests)
  security/SecurityFilterChainIntegrationTest.java          (11 tests)
  integration/FeatureApiContractIntegrationTest.java        (22 tests — acceptance gate)
  integration/PmVerificationScenariosTest.java              (7 tests — PM sign-off, @Disabled)

backend/src/test/resources/
  test-seed.sql       (idempotent: admin user + ADMINISTRATOR role)
  test-cleanup.sql    (removes test-created rows after class)

frontend/
  vitest.config.ts
  src/setupTests.ts
  src/__tests__/authStore.test.ts      (4 tests)
  src/__tests__/apiModules.test.ts     (8 tests)
  src/__tests__/Login.test.tsx         (3 tests)

frontend/package.json — added test scripts + vitest/testing-library devDeps
```

---

## Verification Status by Feature

| Feature | Backend | Frontend | E2E Contract Proven |
|---------|---------|----------|---------------------|
| JWT token ops | ✅ | ✅ | ✅ |
| Security filter chain | ✅ | ✅ | ✅ |
| Login / Auth | ✅ | ✅ | ✅ contract; ⚠️ real login blocked by BUG-1 |
| User CRUD | ✅ | ✅ | ✅ contract; ⚠️ search broken (MISMATCH-1) |
| Role CRUD | ✅ | ✅ | ✅ contract; ⚠️ search broken (MISMATCH-1) |
| Authority tree | ✅ | ✅ | ✅ |
| Equipment CRUD + export | ✅ | ✅ | ✅ contract; ⚠️ search broken (MISMATCH-1) |
| Document CRUD + upload | ✅ | ✅ | ✅ contract; ⚠️ download path broken (MISMATCH-3) |
| Access Logs | ✅ | ✅ | ✅ |
| Stats by role | ✅ | ✅ | ✅ contract; ⚠️ real query blocked by BUG-1 |
| Online users | ✅ | ✅ | ⚠️ path mismatch (MISMATCH-2) |

---

## Commands to Reproduce

```bash
# Backend tests (contract gate — must all pass before t14 sign-off)
cd backend && mvn test -Dspring.profiles.active=test

# Backend PM scenarios (activate by removing @Disabled after bugs fixed)
cd backend && mvn test -Dspring.profiles.active=test -Dtest=PmVerificationScenariosTest

# Frontend unit/component tests
cd frontend && npm test
```

---

## Blockers for t14 PM Sign-off

1. **BUG-1/2:** `@Transactional(readOnly = true)` missing on UserService + RoleService read methods → real login fails at production runtime
2. **MISMATCH-1:** Search param `q` vs `name` — frontend search silently broken across 4 controllers
3. **MISMATCH-2:** `/online` vs `/online-users` path — online users page returns 404
4. **MISMATCH-3:** Document download `/{id}/manual` vs `/manual/{filename}` — manual download broken
5. **REGRESSION:** File upload 100 MB → 20 MB — product decision required (Option A or B above)
6. **PM scenarios:** 7 `@Disabled` tests must be activated and pass green before sign-off
