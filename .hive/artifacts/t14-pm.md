# t14 PM Artifact — Feature Parity Verification & Sign-Off

**Task:** t14 | **Role:** PM | **Status:** BLOCKED — Sign-Off NOT Granted  
**Assessment date:** 2026-03-13

---

## Scope Assessment

[scope] Read-only parity audit of a Spring Boot + React rewrite against a Struts2/EasyUI original (files: ~80 original + ~25 new backend + ~30 new frontend; subsystems: auth, user/role/authority mgmt, equipment, documents, logs, stats, online users). Read-only task — exempt from file-count escalation limit.

---

## Executive Summary

**The rewrite cannot be signed off.** The frontend layer (React/Ant Design) is production-ready and faithfully covers all user-facing flows. The backend feature layer — every controller, service, repository, entity, and database migration beyond the scaffold skeleton — was never implemented. All 30 business API endpoints return HTTP 404. No user can log in. No data can be created, read, updated, or deleted.

This is not a partial parity issue or a minor gap. The backend is a scaffold with zero business logic. The frontend is waiting for a backend that does not exist.

---

## Parity Verdict by Module

| Module | Frontend | Backend | DB Schema | E2E Proven | Verdict |
|--------|----------|---------|-----------|------------|---------|
| Authentication (login / register / logout) | ✅ Full UI | ❌ No controller, stub UserDetailsService | ❌ No TUSER schema | ❌ | ❌ NOT DONE |
| User Management (CRUD + batch role assign) | ✅ Full UI | ❌ No UserController or UserService | ❌ | ❌ | ❌ NOT DONE |
| Role Management (CRUD + authority assign) | ✅ Full UI | ❌ No RoleController or RoleService | ❌ | ❌ | ❌ NOT DONE |
| Authority Management (hierarchical CRUD) | ✅ Full UI | ❌ No AuthorityController or service | ❌ | ❌ | ❌ NOT DONE |
| Equipment Management (CRUD + Excel export) | ✅ Full UI | ❌ No EquipmentController or service | ❌ | ❌ | ❌ NOT DONE |
| Document Management (CRUD + file upload) | ✅ Full UI | ❌ No DocumentController or service | ❌ | ❌ | ❌ NOT DONE |
| Access Log Viewer (read-only) | ✅ Full UI | ❌ No LogController, no AOP aspect | ❌ | ❌ | ❌ NOT DONE |
| User Statistics Chart | ✅ Full UI (Recharts) | ❌ No stats endpoint | ❌ | ❌ | ❌ NOT DONE |
| Online Users Monitor | ⚠️ Stub page only | ❌ No endpoint | ❌ | ❌ | ❌ NOT DONE |
| Navigation Menu from DB | ❌ Hardcoded sidebar | ❌ No menu endpoint | ❌ | ❌ | ❌ NOT DONE |
| JWT auth + security filter chain | N/A | ✅ Scaffold present, tested | N/A | Partial | ⚠️ PARTIAL |
| Health endpoint | N/A | ✅ Implemented, tested | N/A | ✅ | ✅ DONE |

---

## Acceptance Criteria Status

### 1. Authentication & Session Management

| Criterion | Status | Evidence |
|-----------|--------|---------|
| Login with valid credentials grants access | ❌ | POST /api/auth/login → 404 (no AuthController) |
| Login with invalid credentials shows failure message | ❌ | No endpoint |
| Register creates a unique account | ❌ | POST /api/auth/register → 404 |
| Username `admin` cannot be registered | ❌ | No business logic exists |
| Logout destroys session / clears JWT | ❌ | POST /api/auth/logout → 404 |
| Session expires (or JWT expires) | ⚠️ | JWT expiry wired, but no valid login path exists |
| Non-admin users blocked from unauthorized endpoints | ⚠️ | Security filter chain tested; but no user persistence layer |

### 2. User Management

| Criterion | Status |
|-----------|--------|
| User list loads with pagination | ❌ |
| Sortable by username | ❌ |
| Search filters by username (partial match) | ❌ |
| Add user with hashed password and role assignment | ❌ |
| Username `admin` cannot be added via interface | ❌ |
| Edit user — name, password, roles | ❌ |
| Admin user (cid=0) cannot be edited or deleted | ❌ |
| Delete with confirmation | ❌ |
| Batch role assignment | ❌ |
| Password displayed as `******` in grid | ❌ |

### 3. Role Management

| Criterion | Status |
|-----------|--------|
| Role list loads with pagination and sorting | ❌ |
| Add role with name, description, authority selection | ❌ |
| Edit updates name, description, authority assignments | ❌ |
| Delete with confirmation | ❌ |
| Authority selection uses hierarchical tree | ✅ (frontend UI present) |

### 4. Authority Management

| Criterion | Status |
|-----------|--------|
| Authority tree displayed hierarchically | ✅ (frontend UI present) |
| Add authority with name, URL, description, seq, parent | ✅ (frontend UI present) |
| Edit updates all authority fields | ✅ (frontend UI present) |
| Delete with confirmation | ✅ (frontend UI present) |
| URL field used for access control enforcement | ❌ (no backend RBAC evaluation) |

### 5. Equipment Management

| Criterion | Status |
|-----------|--------|
| Equipment list loads with pagination and sorting | ❌ |
| Search filters by name (partial match) | ❌ |
| Add equipment with model (required), name, producer, qty, description | ✅ (frontend UI) |
| Edit updates all fields | ✅ (frontend UI) |
| Delete with confirmation | ✅ (frontend UI) |
| Export generates valid `.xls` file (styled headers, all records) | ❌ (no backend, Apache POI unused) |

### 6. Document Management

| Criterion | Status |
|-----------|--------|
| Document list loads with pagination and sorting | ❌ |
| Search filters by name (partial match) | ❌ |
| Add document with model (required), name, producer, qty | ✅ (frontend UI) |
| Edit updates all fields | ✅ (frontend UI) |
| Delete with confirmation | ✅ (frontend UI) |
| Upload manual attaches file; filename stored in record | ❌ |
| Manual appears as clickable download link | ✅ (frontend renders link; backend 404) |
| File upload up to 20 MB (config says 20 MB vs original 100 MB) | ⚠️ Config present but endpoint absent |

### 7. Access Log Viewer

| Criterion | Status |
|-----------|--------|
| Log list loads with pagination and sorting | ❌ |
| Search filters by username | ❌ |
| Login success/failure generates log entry | ❌ (no AOP aspect, no login endpoint) |
| Registration attempt generates log entry | ❌ |
| Log entries are read-only | ✅ (frontend has no edit/delete controls) |

### 8. User Statistics Chart

| Criterion | Status |
|-----------|--------|
| Chart displays user count grouped by role | ❌ (no stats endpoint) |
| Chart renders as bar chart with labeled axes | ✅ (Recharts component implemented; ADR-8 accepted) |
| Original was server-side JPEG; new is client-side JSON | ✅ (ADR-8 approved this change) |

### 9. Online Users Monitor

| Criterion | Status |
|-----------|--------|
| Online panel/page shows currently logged-in users | ❌ (OnlineUsers.tsx is a stub: "Implementation pending") |
| Count matches displayed rows | ❌ |
| Refreshes after successful login | ❌ |
| User appears after login, disappears after logout | ❌ |

### 10. Navigation Menu

| Criterion | Status |
|-----------|--------|
| Menu tree loaded from database (TMENU) | ❌ (sidebar is hardcoded static navigation) |
| All nodes expandable | ✅ (sidebar collapses) |
| Tab management (open, reuse, close, context menu) | ❌ (original EasyUI tab behavior; new system uses page routing — deliberate redesign) |

---

## What Is Preserved (Confirmed)

The following elements of the original system are faithfully represented in the rewrite design, even if not yet proven end-to-end:

| Preserved Element | Where |
|-------------------|-------|
| All 7 functional modules as named, routed pages | `frontend/src/routes/index.tsx`, all page components |
| Full CRUD UX patterns (paginated table, add/edit modals, batch delete) | `DataTable.tsx`, `ModalForm.tsx`, all management pages |
| Role assignment via UI (Transfer widget for users, Tree for roles) | `UserManagement.tsx`, `RoleManagement.tsx` |
| Equipment model, name, producer, quantity, description fields | `EquipManagement.tsx`, `types/index.ts` |
| Document model, name, producer, quantity, manual filename fields | `DocManagement.tsx`, `types/index.ts` |
| Manual upload flow (file picker → POST to backend → shows download link) | `DocManagement.tsx` |
| Excel export trigger button on equipment page | `EquipManagement.tsx` |
| Access log columns: time, user, IP, message | `LogViewer.tsx` |
| User-role stat chart by role | `UserStats.tsx` |
| JWT auth guard on all protected routes | `routes/index.tsx` (RequireAuth) |
| BCrypt password hashing intention | ADR-4 |
| REST pagination contract (page, size, sort, content, totalElements) | `types/index.ts` PageRequest/PageResult |

---

## What Is Missing or Unproven

### Critical — Blocks All User Flows

1. **No `AuthController`** — Login, register, logout all return 404. Zero users can authenticate.
2. **No `UserDetailsService` implementation** — The existing class throws `UsernameNotFoundException` on every call. Even a valid JWT from a future login cannot load user details.
3. **No `UserController`, `RoleController`, `AuthorityController`** — All user/role/authority CRUD returns 404.
4. **No `EquipmentController`, `DocumentController`** — All equipment and document operations return 404.
5. **No `LogController`**, **no `AccessLogAspect`** — Log viewer shows nothing; no audit trail written for any event.
6. **No stats endpoint** — `/api/users/stats/by-role` returns 404; UserStats chart shows error state.
7. **No database schema** — `V1__create_initial_schema.sql` was never written. The app will fail to start against a real MySQL instance (`ddl-auto: validate` + empty schema = startup failure).
8. **No seed data** — The original system had 23 equipment items and 16 document records loaded via `RepairListener`. The rewrite has no equivalent migration.

### Significant — Features Present in Original But Absent in Rewrite

9. **Online Users Monitor** — `OnlineUsers.tsx` renders only a placeholder: _"Implementation pending (t10–t12)"_. The original had a live, always-visible panel. No backend endpoint for `/api/online-users` was designed or implemented.
10. **Navigation menu from database** — Original loaded `TMENU` from DB. New system has a hardcoded static sidebar. No `MenuController` or `GET /api/menus/tree` endpoint was implemented.
11. **Admin account protection** — Business rules "username `admin` cannot be added or registered", "admin user cannot be edited or deleted" exist nowhere in the backend.

### Intentional Scope Changes (Accepted, Documented)

| Original Behavior | New Behavior | Decision |
|---|---|---|
| MD5 password hashing | BCrypt | ADR-4 |
| Session-based auth (30-min timeout) | JWT with configurable expiry | ADR-9 |
| Server-side JPEG chart (JFreeChart) | Client-side JSON + Recharts | ADR-8 |
| `RepairListener` startup wipe + repair | Flyway migrations | ADR-5 |
| Struts2 action URL pattern | REST endpoints | ADR-3 |
| Tab-based EasyUI panel layout | Page-routing sidebar layout | ADR-8 / implicit redesign |
| Calendar widget | Not in scope | No ADR needed |
| Right-click context menus on rows | Button actions in each row | No ADR needed |
| HTTP 404 for unauthorized (same as not found) | HTTP 401/403 separation | ADR (implied by Spring Security) |
| File max size 100 MB | Configured at 20 MB | ⚠️ Config mismatch — needs resolution |

---

## API Contract Gap (All 404)

Every endpoint expected by the frontend is unimplemented in the backend:

| Endpoint | Expected by Frontend | Backend Status |
|----------|---------------------|----------------|
| `POST /api/auth/login` | Login.tsx | ❌ 404 |
| `POST /api/auth/register` | (future) | ❌ 404 |
| `POST /api/auth/logout` | (future) | ❌ 404 |
| `GET /api/users` | UserManagement.tsx | ❌ 404 |
| `POST /api/users` | UserManagement.tsx | ❌ 404 |
| `PUT /api/users/{id}` | UserManagement.tsx | ❌ 404 |
| `DELETE /api/users?ids=` | UserManagement.tsx | ❌ 404 |
| `GET /api/users/all` | UserManagement.tsx | ❌ 404 |
| `PUT /api/users/roles` | UserManagement.tsx | ❌ 404 |
| `GET /api/users/stats/by-role` | UserStats.tsx | ❌ 404 |
| `GET /api/roles` | RoleManagement.tsx | ❌ 404 |
| `GET /api/roles/all` | UserManagement.tsx | ❌ 404 |
| `POST /api/roles` | RoleManagement.tsx | ❌ 404 |
| `PUT /api/roles/{id}` | RoleManagement.tsx | ❌ 404 |
| `DELETE /api/roles?ids=` | RoleManagement.tsx | ❌ 404 |
| `PUT /api/roles/{id}/authorities` | RoleManagement.tsx | ❌ 404 |
| `GET /api/authorities/tree` | AuthorityManagement.tsx, RoleManagement.tsx | ❌ 404 |
| `POST /api/authorities` | AuthorityManagement.tsx | ❌ 404 |
| `PUT /api/authorities/{id}` | AuthorityManagement.tsx | ❌ 404 |
| `DELETE /api/authorities/{id}` | AuthorityManagement.tsx | ❌ 404 |
| `GET /api/equipment` | EquipManagement.tsx | ❌ 404 |
| `POST /api/equipment` | EquipManagement.tsx | ❌ 404 |
| `PUT /api/equipment/{id}` | EquipManagement.tsx | ❌ 404 |
| `DELETE /api/equipment?ids=` | EquipManagement.tsx | ❌ 404 |
| `GET /api/equipment/export` | EquipManagement.tsx | ❌ 404 |
| `GET /api/documents` | DocManagement.tsx | ❌ 404 |
| `POST /api/documents` | DocManagement.tsx | ❌ 404 |
| `PUT /api/documents/{id}` | DocManagement.tsx | ❌ 404 |
| `DELETE /api/documents?ids=` | DocManagement.tsx | ❌ 404 |
| `POST /api/documents/{id}/manual` | DocManagement.tsx | ❌ 404 |
| `GET /api/documents/manual/{filename}` | DocManagement.tsx | ❌ 404 |
| `GET /api/logs` | LogViewer.tsx | ❌ 404 |
| `GET /api/online-users` | OnlineUsers.tsx | ❌ 404 |
| `GET /api/menus/tree` | (Sidebar) | ❌ 404 |

---

## Open Items Requiring Resolution Before Sign-Off

The following items were flagged in t2 as needing explicit verification. None can be verified until the backend is implemented:

| Item | Original t2 Flag | Current Status |
|------|-----------------|---------------|
| Password encryption applied consistently | [notify:tester] | ❌ No backend to test |
| Excel export contains all records (not paginated) | [notify:tester] | ❌ No export endpoint |
| File upload overwrite behavior | [notify:tester] | ❌ No upload endpoint |
| Online users upsert for same user at multiple IPs | [notify:tester] | ❌ No online users implementation |
| AOP logging scope (login + register only) | [notify:tester] | ❌ No AOP aspect |
| repairAction endpoints security posture | [notify:tester] | ✅ Dropped per ADR-5 (Flyway replaces it) |
| 401 vs 404 for unauthorized | [notify:tester] | ✅ New system returns proper 401 (Spring Security confirmed in t13 tests) |
| Chart file accumulation cleanup | [notify:tester] | ✅ Not applicable — ADR-8, client-side charts |
| File upload max size: original 100 MB, config says 20 MB | New | ⚠️ Needs product decision |

---

## Unverified Behaviors Requiring Explicit Proof

[notify:tester] The following behaviors cannot be validated until the backend is implemented. They must be re-tested in any future t13 re-run:

1. Login flow: valid credentials → JWT returned → frontend stores token → protected page accessible
2. Admin super-user bypass: admin role bypasses authority URL checks
3. Excel export: correct columns, all records (not page-limited), `.xls` format, styled header
4. Document manual upload: file stored outside webapp root (ADR-7), filename sanitized (spaces removed), overwrite behavior
5. AOP access logging: login success, login failure, register success, register failure — each generates exactly one log entry with correct username, IP, and message
6. Online users: user appears after login, disappears after logout, count is accurate

---

## Sign-Off Decision

**❌ SIGN-OFF NOT GRANTED**

### Reason

Feature parity cannot be assessed — the backend feature layer does not exist. This is not a gap in a single module; it is the absence of the entire business logic tier. The rewrite has:

- **Frontend**: Production-ready. All 9 routable pages implemented with correct field sets, pagination, modals, file upload/download wiring, and chart rendering. API contracts correctly defined. Auth guards in place. 15/15 frontend tests pass.
- **Backend**: Scaffold skeleton only. JWT infrastructure present and tested. Zero business endpoints. Zero entities beyond `BaseEntity`. Zero database schema. UserDetailsService throws on every call.

### Conditions for Sign-Off

Sign-off will be granted when all of the following are true:

1. ✅ t7 re-delivered: `AuthController`, `UserController`, `RoleController`, `AuthorityController` + entities (`User`, `Role`, `Authority`, join tables) + `UserDetailsService` (JPA-backed, BCrypt) + all services and repositories
2. ✅ t8 re-delivered: `EquipmentController`, `DocumentController` + entities + Apache POI Excel export + file upload/download
3. ✅ t9 re-delivered: `AccessLogAspect` (AOP on login/register), `LogController`, stats endpoint (`/api/users/stats/by-role`), `UserRoleStat` projection
4. ✅ t5 re-delivered: `V1__create_initial_schema.sql` (all 9 tables: TUSER, TROLE, TAUTH, TUSERTROLE, TROLETAUTH, TEQUIP, TDOC, TLOG, TMENU) + `V2__seed_data.sql` (admin user, default roles, demo equipment and documents)
5. ✅ OnlineUsers feature: backend endpoint + frontend implementation (currently a stub)
6. ✅ All 22 contract tests in `FeatureApiContractIntegrationTest` pass green
7. ✅ File upload size limit resolved: 20 MB (new config) vs. 100 MB (original spec) — needs product decision or alignment
8. ✅ End-to-end login → CRUD flow manually or automatically verified

### What Does NOT Block Sign-Off (Intentional Changes)

The following differences from the original are accepted and do not block sign-off, as they are covered by ADRs or represent agreed UI modernization:

- BCrypt replaces MD5 (ADR-4)
- JWT replaces session auth (ADR-9)
- Client-side Recharts replaces server-side JFreeChart JPEG (ADR-8)
- Flyway migrations replace RepairListener startup wipe (ADR-5)
- REST API replaces Struts2 action URLs (ADR-3)
- Sidebar layout replaces EasyUI tab panel layout
- Standard button actions replace right-click context menus
- RepairAction utility endpoints dropped

---

## Test Evidence Summary

| Area | Tests | Pass | Fail | Conclusion |
|------|-------|------|------|------------|
| JWT token operations | 7 | 7 | 0 | ✅ JWT infra correct |
| Spring Security filter chain | 11 | 11 | 0 | ✅ Security wiring correct |
| Health endpoint | 2 | 2 | 0 | ✅ App starts |
| Feature API contracts | 22 | 0 | 22 | ❌ All business endpoints absent |
| Frontend store (authStore) | 4 | 4 | 0 | ✅ Auth state machine correct |
| Frontend API module shape | 8 | 8 | 0 | ✅ Contract wiring correct |
| Frontend Login component | 3 | 3 | 0 | ✅ Login UI correct |
| **Total** | **57** | **35** | **22** | **❌ 39% of feature tests fail** |
