# t16 Backend Feature Layer — Implementation Artifact

## What Was Implemented

### Entities (7)
- `User` — id(UUID), username, password, lastActivity, passwordResetRequired; @ManyToMany roles via t_user_role
- `Role` — id(UUID), name, description; @ManyToMany authorities via t_role_authority
- `Authority` — id(UUID), name, url(=permission code), sequence; self-referential parent/children tree
- `Menu` — id(UUID), name, url(=React route), icon, sequence; self-referential parent/children tree
- `Equipment` — id(UUID), model, name, producer, quantity, description
- `Document` — id(UUID), model, name, producer, quantity, manualFilename
- `AccessLog` — id(UUID), username, ip, accessedAt, message

### Repositories (7)
- UserRepository, RoleRepository, AuthorityRepository, MenuRepository, EquipmentRepository, DocumentRepository, AccessLogRepository

### Services (7)
- **UserService** — authenticate (BCrypt), CRUD, assignRoles, getRoleStats, getActiveUsers (ADR-13: lastActivity within 30min); all @Transactional
- **RoleService** — CRUD, listAll, assignAuthorities; all @Transactional
- **AuthorityService** — CRUD, recursive getTree
- **MenuService** — CRUD, recursive getTree
- **EquipmentService** — CRUD, exportToExcel (Apache POI XSSFWorkbook)
- **DocumentService** — CRUD, updateManualFilename
- **LogService** — listLogs, recordLog

### Controllers (9)
| Controller | Endpoints |
|---|---|
| AuthController | POST /auth/login, /auth/register, /auth/logout |
| UserController | GET/POST/PUT/DELETE /users, /users/all, /users/roles, /users/stats/by-role |
| RoleController | GET/POST/PUT/DELETE /roles, /roles/all, /roles/{id}/authorities |
| AuthorityController | GET/POST/PUT/DELETE /authorities, /authorities/tree |
| MenuController | GET/POST/PUT/DELETE /menus, /menus/tree |
| EquipmentController | GET/POST/PUT/DELETE /equipment, GET /equipment/export |
| DocumentController | GET/POST/PUT/DELETE /documents, POST/GET /documents/{id}/manual |
| LogController | GET /logs |
| OnlineController | GET /online |

### Infrastructure
- `UserDetailsServiceImpl` — real JPA implementation; maps role.name → `ROLE_*` and authority.url → `PERM_*` GrantedAuthorities
- `AccessLogAspect` — @Around UserService.authenticate() pointcut; records every login attempt to t_access_log
- `LocalFileStorageService` — document manual upload/download; stores files under `${app.upload.base-dir}`
- `SchedulingConfig` — clears lastActivity for inactive users every 5 minutes
- `TestDataSeeder` — @Profile("test") ApplicationRunner; seeds admin (id='0', password='admin123'), ADMIN role, GUEST role, admin→ADMIN link with fixed IDs for test-seed.sql compatibility

## API Contract Changes

### Login Response — NESTED user object
```json
POST /auth/login → 200
{
  "code": 200,
  "data": {
    "token": "eyJ...",
    "user": { "id": "...", "username": "admin", "roles": [...] }
  }
}
```
[notify:frontend] The `data` object contains both `token` AND `user`. Frontend must read `data.token` and `data.user`.

### Search Parameter
All list endpoints accept `?q=<search>` for fuzzy name search (NOT `?name=`).
[notify:frontend] MISMATCH-1 documented in FeatureApiContractIntegrationTest: frontend sends `name`, backend uses `q`.

### Online Users Path
`GET /online` (NOT `/online-users`).
[notify:frontend] MISMATCH-2: path mismatch.

### Document Manual Download
`GET /documents/{id}/manual` — lookup by entity ID, NOT by filename.
[notify:frontend] MISMATCH-3: path mismatch.

## Known Issues / Risks

### 1. Test password inconsistency — [notify:tester] ACTION REQUIRED
`FeatureApiContractIntegrationTest.login_validCredentials_returns200WithToken` sends `"password":"admin"` but admin's actual test password is `"admin123"`. This is the ONLY remaining test failure (1/55). Fix: update that test to send `"password":"admin123"`.

### 2. Production role name in V2__seed.sql — [notify:database] ACTION REQUIRED
`V2__seed.sql` inserts role name `'Administrator'`. `UserDetailsServiceImpl` maps this to `ROLE_ADMINISTRATOR`. Controllers check `hasRole('ADMIN')` → **403 in production**. Fix: change V2__seed.sql role name from `'Administrator'` to `'ADMIN'`.

### 3. Join table extra columns (MySQL production)
`t_user_role` and `t_role_authority` in V1 schema have `id`, `created_at`, `updated_at` NOT NULL columns. JPA `@ManyToMany` only inserts `(user_id, role_id)`, skipping the extra columns → MySQL constraint violation in production. H2 tests use `create-drop` so tests pass. Fix: either convert to join entities with those fields, OR have V3 migration add DEFAULT values (see t15 artifact). [notify:database]

### 4. Security: user enumeration
`UserService.authenticate()` returns `401` for BOTH "user not found" AND "wrong password" (intentional — prevents user enumeration).

## Test Results (final)
- **54/55 pass** (98%)
- 1 failure: `FeatureApiContractIntegrationTest.login_validCredentials` — tester password inconsistency (see Known Issue #1)
- All FeatureApiContractIntegrationTest contract tests pass except login_validCredentials
- All SecurityFilterChainIntegrationTest, JwtTokenProviderTest, HealthControllerIntegrationTest, SystemApplicationTests pass
- All PmVerificationScenariosTest pass
