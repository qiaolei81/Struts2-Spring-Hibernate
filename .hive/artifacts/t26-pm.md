# t26 PM Artifact — Production RBAC Fix Sign-Off

**Task:** t26 | **Role:** PM | **Status:** ✅ SIGN-OFF GRANTED (with one staging gate)
**Assessment date:** 2026-03-13
**Prior sign-off:** t22 — full feature parity accepted
**This sign-off scope:** V3 migration correctness + production RBAC chain + 55/55 tests

---

## Scope Assessment

[scope] Read-only production-readiness audit: V3 migration file review, RBAC chain verification, test results, and three tester-flagged API contract mismatches. (files: 0 modified, subsystems: 1 — database/auth)

---

## What t24/t25 Delivered

| Deliverable | Status |
|---|---|
| V3 migration file: `V3__fix_role_name.sql` | ✅ Present and correct |
| Git commit: `2f071d80` | ✅ Committed |
| Migration idempotency guard (`WHERE name = 'Administrator'`) | ✅ Confirmed |
| FK integrity (no FK on `t_role.name`) | ✅ Confirmed safe |
| Full test suite re-run: 55/55 pass | ✅ Confirmed |

---

## RBAC Chain Verdict

The production RBAC break was a seed data/code mismatch — not a logic error:

| State | Role name in DB | Authority produced by code | `@PreAuthorize` expects | Result |
|---|---|---|---|---|
| **Before V3** | `Administrator` | `ROLE_ADMINISTRATOR` | `ROLE_ADMIN` | ❌ 403 on all admin endpoints |
| **After V3** | `ADMIN` | `ROLE_ADMIN` | `ROLE_ADMIN` | ✅ 200 granted |

The fix is correct. All 9 controllers' `@PreAuthorize` expressions are verified sound by t25.

**Why tests always passed:** `TestDataSeeder` (test profile) independently seeded role name `'ADMIN'` — masking the V2 seed bug in the test context. This is the correct pattern; the fix is in the right place (Flyway migration, not the test fixture).

---

## Tester-Flagged Mismatches — PM Assessment

t25 reported three pre-existing API contract mismatches. These were not introduced by V3. Assessment:

### MISMATCH-2: Online users path — ✅ RESOLVED (not a defect)

| | Value |
|---|---|
| Reported as | Backend `/online`, frontend `/online-users` |
| Actual state | `OnlineController`: `@GetMapping({"/online", "/online-users"})` — handles **both** paths |
| Verdict | **Not a defect.** Backend accepts both. |

### MISMATCH-3: Document manual download path — ✅ RESOLVED (not a defect)

| | Value |
|---|---|
| Reported as | Backend `GET /documents/{id}/manual`, frontend `GET /documents/manual/{filename}` |
| Actual state | `DocumentController` has **both**: `GET /{id}/manual` AND `GET /manual/{filename}` |
| Frontend uses | `downloadBlob('/documents/manual/${filename}')` → matches `GET /manual/{filename}` |
| Verdict | **Not a defect.** Both patterns implemented. |

### MISMATCH-1: Search query parameter name — ⚠️ KNOWN GAP (post-launch backlog)

| | Value |
|---|---|
| Frontend sends | `?name=<search term>` (in users, roles, equipment, document list calls) |
| Backend expects | `@RequestParam(required = false) String q` (in all four controllers) |
| Runtime effect | Backend receives `name=foo`, ignores it (no matching param), returns **all records** unfiltered |
| Affected flows | Search/filter in User Management, Role Management, Equipment Management, Document Management |
| CRUD impact | None — list, create, edit, delete all function correctly |
| Security impact | None |
| Severity | Medium — search is silent no-op; no error, no data loss, no auth bypass |

**PM decision:** This gap is pre-existing (present since t7/t10 were written), was not introduced by V3, and does not affect any core CRUD flow or security property. It is a **post-launch fix** — one-line change per controller (`String q` → `@RequestParam(name = "name") String q`), plus frontend alignment if needed.

[notify:tester] MISMATCH-1 (search param `name` vs `q`) confirmed as a known post-launch backlog item — medium severity, no CRUD or auth impact. Does not block this sign-off.

### Upload size — ✅ NOT A DEFECT

`app.upload.max-size-bytes: 20971520` (20 MB) in application.yml is configured but **not enforced** — `LocalFileStorageService` performs no size check. The effective limit is `spring.servlet.multipart.max-file-size: 100MB`, which matches the original. `UploadSizeRegression` test passes. No action needed.

---

## Staging Gate (Required Before Production Deploy)

The tester correctly identified one item that cannot be proven in the H2 test context:

> **V3 migration has not been run against a real MySQL instance.**
> `flyway.enabled: false` in `application-test.yml` means all test runs use H2 with schema auto-created — Flyway migrations are never applied during testing.

**Required before deploying to production:**

1. Take a copy of the current production database (or a MySQL instance with V1+V2 applied)
2. Start the application against it — Flyway should auto-apply V3 on startup
3. Verify: `SELECT name FROM t_role WHERE id='0'` returns `'ADMIN'`
4. Smoke test: login as admin, confirm `GET /users` returns 200 (not 403)

This is an operational precaution, not a defect in the migration SQL. The migration is syntactically correct MySQL and idempotent.

---

## Sign-Off Conditions — All Met

| Condition | Status |
|---|---|
| V3 migration file exists, is correct, and is committed | ✅ |
| Migration is idempotent and FK-safe | ✅ |
| RBAC chain `'ADMIN'` → `ROLE_ADMIN` → `hasRole('ADMIN')` is sound | ✅ |
| All 55 tests pass (0 failures, 0 skipped) | ✅ t25 confirmed; t22 ran live |
| MISMATCH-2 and MISMATCH-3 are not real defects | ✅ Both resolved by dual-path/dual-endpoint impl |
| MISMATCH-1 assessed and scoped to post-launch backlog | ✅ Does not block |
| File upload effective limit is 100MB (parity) | ✅ |

---

## ✅ SIGN-OFF GRANTED

**The V3 production RBAC fix is accepted.** The rewrite remains production-ready per the t22 acceptance baseline, with the additional V3 fix now confirmed correct.

**One required gate before go-live:** run V3 against a MySQL staging copy and confirm the `t_role` row is updated. This is not a code defect — it is standard pre-production migration validation.

**Post-launch backlog (one item):**
- Fix search param name mismatch: `@RequestParam(name = "name") String q` across UserController, RoleController, EquipmentController, DocumentController.

---

## Cycle Summary

| Task | Deliverable | Outcome |
|---|---|---|
| t22 | Feature parity sign-off | ✅ Full parity accepted |
| t23 | Git commit + housekeeping | ✅ Clean repo |
| t24 | V3 migration (role name fix) | ✅ Correct and committed |
| t25 | V3 verification + 55/55 tests | ✅ All green; 3 mismatches flagged, 2 already resolved |
| **t26** | **Production RBAC sign-off** | **✅ Accepted — staging gate noted** |
