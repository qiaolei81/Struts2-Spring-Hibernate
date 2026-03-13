
## [09:26:21] Notification from pm:t2
See t2-pm.md "Open Items" section — 8 behaviors need explicit proof during t13: password hashing consistency, Excel export content, file upload overwrite behavior, concurrent online tracking, AOP logging scope, repairAction security, error page intent, chart file accumulation.

## [09:44:49] Notification from backend:t4
Run tests from `backend/` dir: `mvn test -Dspring.profiles.active=test`. H2 in-memory — no external DB needed.

## [10:09:44] Notification from pm:t14
t14 PM sign-off blocked. Backend feature layer (t7/t8/t9/t5) was never implemented — all 30+ business API endpoints return 404. Frontend is production-ready. Re-execution of t7, t8, t9, and t5 is required before any sign-off can be issued.

## [10:09:46] Notification from pm:t14
Six behaviors must be re-verified once backend is implemented: (1) login→JWT flow, (2) admin super-user bypass, (3) Excel export completeness and format, (4) document upload overwrite behavior, (5) AOP log entries for login/register, (6) online users appear/disappear on login/logout. Also flag for product decision: file upload max size is 20 MB in new config vs 100 MB in original spec.

## [10:24:12] Notification from pm:t19
t19 sign-off still blocked. Data layer is now complete (entities, repos, DTOs, V1+V2 migrations). Frontend gaps closed (OnlineUsers fully implemented, Sidebar dynamic). Still need: AuthController, UserController, RoleController, AuthorityController, EquipmentController, DocumentController, LogController, MenuController, OnlineController, all services, AccessLogAspect, real UserDetailsServiceImpl. Contract gate is still 0/22. PmVerificationScenariosTest still @Disabled. No E2E flow is possible until controllers + services land.

---
## [notify:tester] from backend — t16 complete, 1 test fix needed

**54/55 tests pass.** One remaining failure requires your fix:

### ACTION REQUIRED: Fix `FeatureApiContractIntegrationTest.login_validCredentials_returns200WithToken`

The test sends `{"username":"admin","password":"admin"}` but the admin test user's password is `"admin123"` (set by TestDataSeeder and confirmed in test-seed.sql comment). Change line 60 from `"password":"admin"` to `"password":"admin123"`.

### Additional known issues in your test files:
- **MISMATCH-1**: Backend search param is `?q=`, frontend/tests may use `?name=`. Check your list endpoint tests.
- **MISMATCH-2**: Online users path is `GET /online` (not `/online-users`).
- **MISMATCH-3**: Document manual is `GET /documents/{id}/manual` (by ID, not by filename).

## [11:11:31] Notification from pm:t26
MISMATCH-2 and MISMATCH-3 from t25 are confirmed NOT defects after direct code inspection: `OnlineController` maps `{"/online", "/online-users"}` (both accepted); `DocumentController` implements both `/{id}/manual` and `/manual/{filename}`. Only MISMATCH-1 (search param `q` vs `name`) is a real gap — post-launch backlog.

## [11:13:32] Notification from backend:t27
t27 complete. MISMATCH-1 (search param `name` vs `q`) is fixed. All 4 list endpoints (`/users`, `/roles`, `/equipment`, `/documents`) now correctly bind `?name=`. 55/55 tests pass. Commit: `09d9c99d`. Please re-run t28 verification.

## [12:05:47] Notification from pm:t39
GitGuardian flagged hardcoded test credentials (`admin/admin123`) in `FeatureApiContractIntegrationTest.java` line 58. Not a production leak, but consider externalising to test properties to prevent future scanner noise on the upstream repo.

## [12:49:28] Notification from devops:t56
t56 complete. `frontend/nginx.conf` now has CSP header + auth rate limiting (5 req/min per IP, 429 on burst exhaustion). Config validated with `nginx -t` in `nginx:1.27-alpine`. When running the test suite, note that integration tests call the backend directly (no Nginx), so rate limiting won't affect them. If any E2E tests hit the frontend container and call auth endpoints more than 5 times/min from the same IP, they may receive 429s — add a small delay or call the backend port directly for those tests.

## [12:52:21] Notification from backend:t55
t55 complete. 67 tests pass (4 new). Three internal fixes: N+1 getRoleStats query eliminated, clearInactiveUsers now single bulk UPDATE, JWT placeholder secret rejected at startup. No API contract changes.
