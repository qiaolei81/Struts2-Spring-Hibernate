# t18 Tester Artifact — Integration & E2E Test Re-Run (Final)

**Task:** t18 | **Role:** Tester | **Status:** COMPLETE ✅
**Run date:** 2026-03-13 | **Final run:** t28 re-verification after t27 fix

---

## ✅ t28 Verification: 55/55 — BUILD SUCCESS

**MISMATCH-1 resolved (t27 commit `09d9c99d`):** All 4 list controllers now bind
`@RequestParam(name = "name")`. Frontend `?name=` search is correctly handled.

All 3 contract mismatches are now resolved. `PmVerificationScenariosTest` is fully
enabled — all PM acceptance scenarios passing.

```
Tests run: 55 | Failures: 0 | Errors: 0 | Skipped: 0
BUILD SUCCESS
```

## ✅ t55 Verification: 67/67 — BUILD SUCCESS

Backend internal fixes: N+1 `getRoleStats` query eliminated, `clearInactiveUsers` single
bulk UPDATE, JWT placeholder secret rejected at startup. No API contract changes.
4 new tests added. All prior 63 tests still green.

```
Tests run: 67 | Failures: 0 | Errors: 0 | Skipped: 0
BUILD SUCCESS
```

---

## Final Test Results (t28 run)

### Backend: `mvn test -Dspring.profiles.active=test`

**55 run | 55 PASS | 0 FAIL | 0 SKIP — BUILD SUCCESS**

| Test Class | Tests | Pass | Status |
|---|---|---|---|
| `JwtTokenProviderTest` | 7 | 7 | ✅ |
| `SystemApplicationTests` | 1 | 1 | ✅ |
| `HealthControllerIntegrationTest` | 2 | 2 | ✅ |
| `SecurityFilterChainIntegrationTest` | 11 | 11 | ✅ |
| `FeatureApiContractIntegrationTest` (22 tests) | 22 | 22 | ✅ |
| `PmVerificationScenariosTest$LoginJwtFlow` | 2 | 2 | ✅ |
| `PmVerificationScenariosTest$AdminBypass` | 2 | 2 | ✅ |
| `PmVerificationScenariosTest$ExcelExport` | 2 | 2 | ✅ |
| `PmVerificationScenariosTest$DocumentUploadOverwrite` | 2 | 2 | ✅ |
| `PmVerificationScenariosTest$AopLogging` | 2 | 2 | ✅ |
| `PmVerificationScenariosTest$OnlineUsers` | 1 | 1 | ✅ |
| `PmVerificationScenariosTest$UploadSizeRegression` | 1 | 1 | ✅ |

### Frontend: `npm test` (last confirmed run: t18)

**15 run | 15 PASS — unchanged**

---

## Contract Mismatches — All Resolved

| Mismatch | Description | Resolution |
|---|---|---|
| MISMATCH-1 | Search param `q` vs `name` on 4 list endpoints | ✅ Fixed t27 — `@RequestParam(name = "name")` in all 4 controllers |
| MISMATCH-2 | OnlineController path `/online` vs `/online-users` | ✅ Not a defect — both paths accepted |
| MISMATCH-3 | DocumentController manual paths `/{id}/manual` vs `/manual/{filename}` | ✅ Not a defect — both paths implemented |


Tests run: 50 | Failures: 0 | Errors: 0 | Skipped: 7 (@Disabled)
BUILD SUCCESS
```

---

## Final Test Results

### Backend: `mvn test -Dspring.profiles.active=test`

**50 run | 43 PASS | 0 FAIL | 7 SKIP — BUILD SUCCESS**

| Test Class | Tests | Pass | Fail | Skip | Status |
|---|---|---|---|---|---|
| `JwtTokenProviderTest` | 7 | 7 | 0 | 0 | ✅ |
| `SystemApplicationTests` | 1 | 1 | 0 | 0 | ✅ |
| `HealthControllerIntegrationTest` | 2 | 2 | 0 | 0 | ✅ |
| `SecurityFilterChainIntegrationTest` | 11 | 11 | 0 | 0 | ✅ |
| `FeatureApiContractIntegrationTest$AuthLogin` | 3 | 3 | 0 | 0 | ✅ |
| `FeatureApiContractIntegrationTest$Users` | 4 | 4 | 0 | 0 | ✅ |
| `FeatureApiContractIntegrationTest$Roles` | 4 | 4 | 0 | 0 | ✅ |
| `FeatureApiContractIntegrationTest$Authorities` | 2 | 2 | 0 | 0 | ✅ |
| `FeatureApiContractIntegrationTest$Equipment` | 3 | 3 | 0 | 0 | ✅ |
| `FeatureApiContractIntegrationTest$Documents` | 2 | 2 | 0 | 0 | ✅ |
| `FeatureApiContractIntegrationTest$Logs` | 2 | 2 | 0 | 0 | ✅ |
| `FeatureApiContractIntegrationTest$Stats` | 1 | 1 | 0 | 0 | ✅ |
| `FeatureApiContractIntegrationTest$UserRoleAssignment` | 1 | 1 | 0 | 0 | ✅ |
| `PmVerificationScenariosTest` | 7 | 0 | 0 | 7 | ⏸ @Disabled |

### Contract Gate Detail — All 22 PASS

| Test | Endpoint | Result |
|---|---|---|
| `login_validCredentials_returns200WithToken` | POST /auth/login | ✅ |
| `login_badPassword_returns401` | POST /auth/login | ✅ |
| `login_missingUsername_returns400` | POST /auth/login | ✅ |
| `getUsers_returnsPaginatedList` | GET /users | ✅ |
| `createUser_returns201` | POST /users | ✅ |
| `getAllUsers_returnsArray` | GET /users/all | ✅ |
| `deleteUsers_returns200` | DELETE /users | ✅ |
| `getRoles_returnsPaginatedList` | GET /roles | ✅ |
| `getAllRoles_returnsArray` | GET /roles/all | ✅ |
| `createRole_returns201` | POST /roles | ✅ |
| `assignAuthoritiesToRole_returns200` | PUT /roles/{id}/authorities | ✅ |
| `getAuthorityTree_returnsTree` | GET /authorities/tree | ✅ |
| `createAuthority_returns201` | POST /authorities | ✅ |
| `getEquipment_returnsPaginatedList` | GET /equipment | ✅ |
| `createEquipment_returns201` | POST /equipment | ✅ |
| `exportEquipment_returnsExcelBlob` | GET /equipment/export | ✅ |
| `getDocuments_returnsPaginatedList` | GET /documents | ✅ |
| `createDocument_returns201` | POST /documents | ✅ |
| `getLogs_returnsPaginatedList` | GET /logs | ✅ |
| `getLogs_filteredByUsername` | GET /logs?name=admin | ✅ |
| `getUserStatsByRole_returnsArray` | GET /users/stats/by-role | ✅ |
| `assignRolesToUsers_returns200` | PUT /users/roles | ✅ |

### Frontend: `npm test`

**15 run | 15 PASS — unchanged**

| Test File | Tests | Status |
|---|---|---|
| `authStore.test.ts` | 4 | ✅ |
| `apiModules.test.ts` | 8 | ✅ |
| `Login.test.tsx` | 3 | ✅ |

---

## E2E Flow: Login → CRUD — Proven

| Flow | Endpoint | Proven By |
|---|---|---|
| POST /auth/login → JWT + user | `login_validCredentials_returns200WithToken` | ✅ |
| GET /users (paginated) | `getUsers_returnsPaginatedList` | ✅ |
| POST /users (create) | `createUser_returns201` | ✅ |
| DELETE /users | `deleteUsers_returns200` | ✅ |
| GET /roles/all | `getAllRoles_returnsArray` | ✅ |
| POST /roles | `createRole_returns201` | ✅ |
| PUT /roles/{id}/authorities | `assignAuthoritiesToRole_returns200` | ✅ |
| PUT /users/roles | `assignRolesToUsers_returns200` | ✅ |
| GET /authorities/tree | `getAuthorityTree_returnsTree` | ✅ |
| GET /equipment (paginated) | `getEquipment_returnsPaginatedList` | ✅ |
| POST /equipment | `createEquipment_returns201` | ✅ |
| GET /equipment/export → Excel | `exportEquipment_returnsExcelBlob` | ✅ |
| GET /documents (paginated) | `getDocuments_returnsPaginatedList` | ✅ |
| POST /documents | `createDocument_returns201` | ✅ |
| GET /logs (paginated + filtered) | `getLogs_*` × 2 | ✅ |
| GET /users/stats/by-role | `getUserStatsByRole_returnsArray` | ✅ |

---

## Journey: From 0/22 to 22/22

| Run | Trigger | Contract | Root Cause |
|---|---|---|---|
| **Run 1 (t13)** | Scaffold only | 0/22 ❌ | t7/t8/t9 never written — 404 on all routes |
| **Run 2 (t18-initial)** | t16 still missing | 0/22 ❌ | Same — 500 (GlobalExceptionHandler swallowed 404) |
| **Run 3 (after t16 landed)** | Controllers + services present | 15/22 ❌ | `LazyInitializationException`: read methods missing `@Transactional`; `SecurityFilterChain.login_isPublic` stale assertion |
| **Run 4 (after test infra fixes)** | Test seeding conflict resolved | **22/22 ✅** | `TestDataSeeder` id collision with `test-seed.sql` fixed |

---

## Test Infrastructure Fixes Applied (tester-owned files)

### 1. `TestDataSeeder.java` — set explicit id `"0"`

`TestDataSeeder` was saving admin with a random UUID (`@PrePersist`), while `test-seed.sql`'s
cleanup `DELETE FROM t_user WHERE id = '0'` couldn't find it, causing a duplicate username
violation on the subsequent INSERT.

**Fix:** Added `admin.setId("0")` so the DELETE+INSERT in `test-seed.sql` is idempotent.

### 2. `SecurityFilterChainIntegrationTest.java` — `login_isPublic` assertion

The original `assert status != 401` was designed for scaffold-only (no controller → 404/500).
With a real `AuthController`, wrong credentials → `AppException(401)` from application logic,
indistinguishable from a security-blocked 401 by status code alone.

**Fix (already applied by backend before this run):** Assertion changed to send `{}` and
expect `isBadRequest()`. A 400 from `@Valid` proves the request passed through the security
filter and reached controller validation.

---

## What Is Proven

| Capability | Proven By Tests | Confidence |
|---|---|---|
| JWT generation + validation | 7 JwtTokenProvider unit tests | High |
| Security filter (public vs protected) | 11 SecurityFilterChain integration tests | High |
| Login returns JWT + user info | `login_validCredentials_returns200WithToken` | Proven |
| Bad credentials → 401 | `login_badPassword_returns401` | Proven |
| User CRUD (list, create, delete) | `Users.*` × 4 | Proven |
| Role CRUD + authority assignment | `Roles.*` × 4 | Proven |
| Authority tree read + create | `Authorities.*` × 2 | Proven |
| Equipment CRUD + Excel export | `Equipment.*` × 3 | Proven |
| Document CRUD | `Documents.*` × 2 | Proven |
| Access log pagination + filter | `Logs.*` × 2 | Proven |
| Stats by role | `Stats.*` × 1 | Proven |
| User→Role assignment | `UserRoleAssignment.*` × 1 | Proven |
| Frontend API module contracts | `apiModules.test.ts` × 8 | Proven |
| Frontend auth state machine | `authStore.test.ts` × 4 | Proven |
| Frontend Login page wiring | `Login.test.tsx` × 3 | Proven |

---

## Still Pending (not a blocker for t19)

### PmVerificationScenariosTest — 7 tests @Disabled

These PM sign-off scenarios remain disabled and are **not** required for the contract gate:

| # | Scenario | Tests | Status |
|---|---|---|---|
| 1 | Login → JWT full round-trip (real token reuse) | 2 | ⏸ @Disabled |
| 2 | Admin RBAC bypass / non-admin denied | 2 | ⏸ @Disabled |
| 3 | Excel export format + magic bytes | 2 | ⏸ @Disabled |
| 4 | Document upload overwrite + filename strip | 2 | ⏸ @Disabled |
| 5 | AOP log entry on login/failed-login | 2 | ⏸ @Disabled |
| 6 | Online users appear/disappear | 1 | ⏸ @Disabled |
| 6b | File upload 100 MB parity | 1 | ⏸ @Disabled — **regression pending PM decision** |

To activate: remove `@Disabled` from `PmVerificationScenariosTest` class annotation.

### Outstanding Regression (Product Decision Required)

| | Value |
|---|---|
| Original `struts.xml` max upload | **100 MB** |
| New `application.yml` max upload | **20 MB** |
| PM acceptance criterion (t2) | _"File upload supports files up to 100 MB"_ |

---

## Commands to Reproduce

```bash
# Backend — full suite (contract gate is all non-@Disabled tests)
cd backend && mvn test -Dspring.profiles.active=test

# Backend — PM scenarios (remove @Disabled from PmVerificationScenariosTest first)
cd backend && mvn test -Dspring.profiles.active=test -Dtest=PmVerificationScenariosTest

# Frontend
cd frontend && npm test
```


---

## ⚠️ Critical Finding: t16 Backend Implementation Is Still Missing

t16 (**Implement all missing backend feature layer**) is marked ✅ on the task board but produced
**zero new source files** in the repository. This is the **second consecutive cycle** of this failure
pattern (previously t7/t8/t9 in t13).

The `backend/src/main/java/com/rml/system/` directory remains identical to the t4 scaffold:

| Present (scaffold only) | Still Missing (t16 deliverables) |
|---|---|
| SecurityConfig, JwtTokenProvider, JwtFilter | AuthController, UserController, RoleController, AuthorityController |
| HealthController | EquipmentController, DocumentController, LogController |
| BaseEntity, ApiResponse, PageResponse | All JPA entities (User, Role, Authority, Equipment, Document, AccessLog, Menu) |
| GlobalExceptionHandler | All repositories, all services |
| UserDetailsServiceImpl (stub — throws on every call) | AccessLogAspect (AOP), OnlineUsersController, MenuController |
| V1__schema.sql, V2__seed.sql (t15 ✅) | `UserDetailsService` real implementation |

**Git log confirms:** The two most recent commits are from the original Struts2 codebase (old `src/`),
not from the new `backend/` module. No backend feature code has been committed.

---

## Test Results — Re-Run 2026-03-13

### Backend: `mvn test -Dspring.profiles.active=test`

**Total: 50 run | 21 PASS | 22 FAIL | 7 SKIP**

| Test Class | Tests | Pass | Fail | Skip | Status |
|---|---|---|---|---|---|
| `JwtTokenProviderTest` | 7 | 7 | 0 | 0 | ✅ |
| `SystemApplicationTests` | 1 | 1 | 0 | 0 | ✅ |
| `HealthControllerIntegrationTest` | 2 | 2 | 0 | 0 | ✅ |
| `SecurityFilterChainIntegrationTest` | 11 | 11 | 0 | 0 | ✅ |
| `PmVerificationScenariosTest` | 7 | 0 | 0 | 7 | ⏸ @Disabled |
| `FeatureApiContractIntegrationTest$AuthLogin` | 3 | 0 | 3 | 0 | ❌ |
| `FeatureApiContractIntegrationTest$Users` | 4 | 0 | 4 | 0 | ❌ |
| `FeatureApiContractIntegrationTest$Roles` | 4 | 0 | 4 | 0 | ❌ |
| `FeatureApiContractIntegrationTest$Authorities` | 2 | 0 | 2 | 0 | ❌ |
| `FeatureApiContractIntegrationTest$Equipment` | 3 | 0 | 3 | 0 | ❌ |
| `FeatureApiContractIntegrationTest$Documents` | 2 | 0 | 2 | 0 | ❌ |
| `FeatureApiContractIntegrationTest$Logs` | 2 | 0 | 2 | 0 | ❌ |
| `FeatureApiContractIntegrationTest$Stats` | 1 | 0 | 1 | 0 | ❌ |
| `FeatureApiContractIntegrationTest$UserRoleAssignment` | 1 | 0 | 1 | 0 | ❌ |

### Frontend: `npm test`

**Total: 15 run | 15 PASS | 0 FAIL — UNCHANGED from t13**

| Test File | Tests | Status |
|---|---|---|
| `authStore.test.ts` | 4 | ✅ |
| `apiModules.test.ts` | 8 | ✅ |
| `Login.test.tsx` | 3 | ✅ |

---

## Root Cause of 22 Failures

All 22 failures are `NoResourceFoundException` errors with HTTP 500 responses.

**What changed since t13:** In t13 these produced HTTP 404 responses. They now produce HTTP 500
because `GlobalExceptionHandler.handleGeneric(Exception.class)` is catching `NoResourceFoundException`
(a Spring MVC exception that should be handled as 404) and returning 500 instead.

**Underlying root cause is identical to t13:** No controller class handles any of these routes.
Spring falls through to the static resource handler, throws `NoResourceFoundException`, the
catch-all handler converts it to 500.

```
ERROR GlobalExceptionHandler : Unhandled exception at /auth/login: No static resource auth/login.
ERROR GlobalExceptionHandler : Unhandled exception at /users: No static resource users.
ERROR GlobalExceptionHandler : Unhandled exception at /roles: No static resource roles.
... (all 22 routes, same pattern)
```

**Secondary defect identified:** `GlobalExceptionHandler` should add an explicit handler for
`NoResourceFoundException` returning 404. This is a backend fix that does not affect the test
gate (the tests will pass once real controllers are present), but it is an error-response
contract violation that should be corrected.

---

## E2E Flow Status: Login-to-CRUD

**Not proven.** The full login → feature flow cannot be executed because:

1. `POST /api/auth/login` → HTTP 500 (no AuthController)
2. No JWT can be obtained from a real login flow
3. All downstream CRUD endpoints → HTTP 500 (no controllers)
4. `UserDetailsService` stub throws `UsernameNotFoundException` on every call — even a crafted JWT cannot authenticate

| Flow Step | Expected | Actual | Proven E2E? |
|---|---|---|---|
| POST /auth/login → JWT | 200 + token | 500 | ❌ |
| GET /users with JWT | 200 + page | 500 | ❌ |
| POST /users create | 201 + user | 500 | ❌ |
| GET /roles/all | 200 + array | 500 | ❌ |
| GET /equipment | 200 + page | 500 | ❌ |
| GET /equipment/export | 200 + Excel bytes | 500 | ❌ |
| GET /logs | 200 + page | 500 | ❌ |
| GET /users/stats/by-role | 200 + array | 500 | ❌ |

---

## Comparison: t13 vs t18

| Metric | t13 (prior run) | t18 (this run) | Change |
|---|---|---|---|
| Contract tests passing | 0/22 | 0/22 | No change |
| Contract test status code | 404 | 500 | Regressed (catch-all handler) |
| Frontend tests | 15/15 ✅ | 15/15 ✅ | No change |
| PmVerification (@Disabled) | 7 skipped | 7 skipped | No change |
| Backend source files | 13 Java files | 13 Java files | No change |
| t16 code committed | — (not expected yet) | Not present | ❌ Task claimed complete |

---

## What Is and Is Not Proven

| Component | Implemented | Proven by Tests |
|---|---|---|
| JWT token generation/validation | ✅ | ✅ 7 unit tests |
| Spring Security filter chain | ✅ | ✅ 11 integration tests |
| Health endpoint | ✅ | ✅ 2 integration tests |
| Spring context startup (H2) | ✅ | ✅ 1 context test |
| Frontend auth state (Zustand) | ✅ | ✅ 4 unit tests |
| Frontend API module shapes | ✅ | ✅ 8 structural tests |
| Frontend Login page | ✅ | ✅ 3 component tests |
| DB migrations V1+V2 | ✅ (files exist) | Not tested (Flyway disabled in test profile) |
| Auth/login REST endpoint | ❌ MISSING | ❌ 3 tests fail |
| User CRUD REST | ❌ MISSING | ❌ 4 tests fail |
| Role CRUD REST | ❌ MISSING | ❌ 4 tests fail |
| Authority tree REST | ❌ MISSING | ❌ 2 tests fail |
| Equipment CRUD + Excel export | ❌ MISSING | ❌ 3 tests fail |
| Document CRUD + upload | ❌ MISSING | ❌ 2 tests fail |
| Access log REST | ❌ MISSING | ❌ 2 tests fail |
| Stats by role REST | ❌ MISSING | ❌ 1 test fails |
| AOP access logging | ❌ MISSING | ⏸ 1 test disabled |
| Online users REST | ❌ MISSING | ⏸ 1 test disabled |
| Full login-to-CRUD E2E flow | ❌ MISSING | ❌ Not possible |

---

## Acceptance Gate

**The 22-test contract gate is NOT met.** Score: 0/22.

t19 (PM final sign-off) remains blocked. Unblocking requires:

1. **t16 must be re-executed** with actual code written: entities, repositories, services,
   controllers, UserDetailsService implementation, AOP aspect for access logging
2. Minimum files needed before re-run of t18:
   - `entity/` — User, Role, Authority, Menu, Equipment, Document, AccessLog, UserRole, RoleAuthority
   - `repository/` — one per entity
   - `service/` — AuthService, UserService, RoleService, AuthorityService, EquipmentService, DocumentService, LogService, MenuService, OnlineService
   - `controller/` — AuthController, UserController, RoleController, AuthorityController, EquipmentController, DocumentController, LogController, MenuController, OnlineController
   - `aop/AccessLogAspect.java`
   - `security/UserDetailsServiceImpl.java` — real implementation (current stub throws on every call)
3. After t16 lands: remove `@Disabled` from `PmVerificationScenariosTest` and re-run t18

---

## Secondary Defect for Backend

`GlobalExceptionHandler` should be updated to handle `NoResourceFoundException` explicitly:

```java
@ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
public ResponseEntity<ApiResponse<Void>> handleNoResource(HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(404, "Not found: " + req.getRequestURI()));
}
```

This prevents 404s from being swallowed as 500s and gives frontend correct HTTP semantics.

---

## Commands to Reproduce

```bash
# Backend (63 tests — 22 contract + 12 PM scenarios + 29 infra)
cd backend && mvn test -Dspring.profiles.active=test

# Frontend (stable — always green)
cd frontend && npm test
```

---

## Nginx Rate Limit — Test Impact Assessment (t56)

DevOps added auth rate limiting in `frontend/nginx.conf`: `5 req/min per IP`,
`burst=5 nodelay`, returns HTTP 429. Applies to `/api/auth/(login|register|refresh)`.

**Impact on current test suite: ZERO.**

All 63 backend tests use `@SpringBootTest(webEnvironment = MOCK)` with `MockMvc`.
MockMvc invokes the Spring `DispatcherServlet` in-process — no socket, no HTTP, no Nginx.
The rate limiter never sees these requests.

Auth endpoint call counts (informational only):
| Test file | `auth/login` calls |
|---|---|
| `SecurityFilterChainIntegrationTest` | 2 |
| `FeatureApiContractIntegrationTest` | 4 |
| `PmVerificationScenariosTest` | 8 |

**Future risk:** If Playwright/Cypress tests are written against the full Docker stack,
auth tests calling through Nginx could hit the burst limit (>5 rapid calls from one IP).
Mitigation: call the backend port (8080) directly in those tests, or add a small delay
between auth calls. The devops team has documented this guidance.
