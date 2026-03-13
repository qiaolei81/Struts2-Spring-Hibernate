
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
