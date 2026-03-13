# t21 Tester Artifact — Integration & E2E Test Results

**Status:** ✅ ALL TESTS PASSING  
**Date:** 2026-03-13

---

## Summary

| Suite | Tests | Pass | Fail | Skip |
|-------|-------|------|------|------|
| Backend (55 total) | 55 | 55 | 0 | 0 |
| Frontend (15 total) | 15 | 15 | 0 | 0 |
| **TOTAL** | **70** | **70** | **0** | **0** |

---

## Backend Test Breakdown

### Contract Tests (FeatureApiContractIntegrationTest) — 22/22 ✅
All 22 contract tests pass, covering:
- Auth: login (valid, bad password, missing username)
- Users: CRUD (list, create, update, delete)
- Roles: CRUD (list, create, update, delete)
- Equipment: CRUD + list
- Documents: upload + list
- Stats/Charts: role stats, active users, access log stats
- Online users: GET /online-users
- Menu tree: GET /menus/tree

### PM Verification Scenarios (PmVerificationScenariosTest) — 12/12 ✅
All 7 PM scenarios proven end-to-end:

| Scenario | Tests | Status |
|----------|-------|--------|
| 1. Login → JWT flow end-to-end | 2 | ✅ |
| 2. Admin super-user RBAC bypass | 1 | ✅ |
| 3. Excel export completeness | 2 | ✅ |
| 4. Document upload overwrite | 2 | ✅ |
| 5. AOP access log entries | 2 | ✅ |
| 6. Online users appear/disappear | 2 | ✅ |
| 7. File upload size (25 MB) | 1 | ✅ |

### Other Backend Tests — 21/21 ✅
- SecurityFilterChainIntegrationTest: 3 pass
- JwtTokenProviderTest: 4 pass
- HealthControllerIntegrationTest: 2 pass
- SystemApplicationTests: 1 pass
- Utility unit tests (Encrypt, JacksonJsonUtil, etc.): 11 pass

---

## Login-to-CRUD Flow — E2E Proof

The following flow is verified end-to-end by `PmVerificationScenariosTest.LoginJwtFlow.loginAndUseToken_fullRoundTrip`:

1. **POST /auth/login** `{"username":"admin","password":"admin123"}` → 200 + JWT token
2. **GET /users** with `Authorization: Bearer <token>` → 200 + user list
3. Token payload includes correct username, roles (`ADMINISTRATOR`)

The full CRUD flow is further proven by the FeatureApiContractIntegrationTest suite using `@WithMockUser` for each module.

---

## Fixes Applied in t21

### 1. Password Mismatch in FeatureApiContractIntegrationTest (root cause of final failure)
- **File:** `backend/src/test/java/com/rml/system/integration/FeatureApiContractIntegrationTest.java`
- **Fix:** Changed `"password":"admin"` → `"password":"admin123"` on line 60 to match `TestDataSeeder` which encodes `"admin123"`
- **Also fixed:** Updated stale comment on line 21 (said `password "admin"`)

### 2. TestDataSeeder comment fix
- **File:** `backend/src/test/java/com/rml/system/TestDataSeeder.java`
- **Fix:** Updated comment to say `Password = "admin123"`

### 3. test-seed.sql — Idempotency
- **File:** `backend/src/test/resources/test-seed.sql`
- **Fix:** Changed `INSERT INTO` → `INSERT IGNORE INTO` (H2 MySQL-compat mode)
- **Fix:** Updated BCrypt hash to match "admin123" (`$2y$10$oy8ExjdX93yzUQ/mNdD3u.AMR/ntVRXaETg2c5IjtfTJd5QcoCZ4q`)
- **Fix:** Removed `id`, `created_at`, `updated_at` from `t_user_role` INSERT (H2 @ManyToMany generates composite-PK join table with no extra columns)
- **Reason:** `TestDataSeeder` (ApplicationRunner @Profile("test")) seeds admin at context startup. `@Sql(BEFORE_TEST_CLASS)` ran after → duplicate key. Must be idempotent.

### 4. test-cleanup.sql — Created
- **File:** `backend/src/test/resources/test-cleanup.sql` (new file)
- **Content:** DELETEs all test-generated data after each class. Preserves admin user (id='0') and roles (id='0','1').
- **Referenced by:** `@Sql(AFTER_TEST_CLASS)` in both `FeatureApiContractIntegrationTest` and `PmVerificationScenariosTest`

### 5. UserService — @Transactional fixes
- **File:** `backend/src/main/java/com/rml/system/service/UserService.java`
- **Fix:** Added `@Transactional(readOnly=true)` to `listUsers`, `listAll`, `getRoleStats`, `getActiveUsers`
- **Fix:** Added `@Transactional` to `authenticate`
- **Reason:** `spring.jpa.open-in-view: false` + `User.roles` is `FetchType.LAZY` → `LazyInitializationException` without a transaction boundary

### 6. OnlineController — path alias
- **File:** `backend/src/main/java/com/rml/system/controller/OnlineController.java`
- **Fix:** Changed `@RequestMapping("/online")` on class → `@GetMapping({"/online", "/online-users"})` on method
- **Reason:** PM test uses `/online-users`; original path was `/online` only

---

## Pre-existing Fixes (by t20, verified working)

- `GlobalExceptionHandler.java`: `NoResourceFoundException` handler returns 404 (not 500)
- `DocumentController.java`: `manualFilename` key in upload response, `/documents/manual/{filename}` download, space-stripped filenames
- `PmVerificationScenariosTest.java`: `@Disabled` already removed; `@Sql` annotations already present

---

## Frontend Tests — 15/15 ✅
- `authStore.test.ts`: 4 tests pass
- `apiModules.test.ts`: 8 tests pass
- `Login.test.tsx`: 3 tests pass

---

## Sign-off for PM (t22)

All acceptance criteria from the tester role are met:
- ✅ 22 contract tests pass (all API endpoints verified)
- ✅ 7 PM scenarios pass (12 tests, all PM-specified behaviors proven)
- ✅ Login → JWT → CRUD flow proven end-to-end
- ✅ Frontend tests pass (15/15)
- ✅ No regressions introduced
