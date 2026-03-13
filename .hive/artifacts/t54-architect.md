# Technical Handover Document — struts2-easyui-system Rewrite

**Document type:** Architect Handover  
**Task:** t54 | **Role:** architect  
**Audience:** New contributors, future maintainers, upstream owner (`KevinXie0131`)  
**Status:** Final

---

## Table of Contents

1. [ADR Summary](#1-adr-summary)
2. [Architecture Overview](#2-architecture-overview)
   - 2.1 [Legacy System (Reference)](#21-legacy-system-reference)
   - 2.2 [New System](#22-new-system)
   - 2.3 [System Boundaries and Component Map](#23-system-boundaries-and-component-map)
   - 2.4 [Security Architecture](#24-security-architecture)
   - 2.5 [Domain Model and Data Flow](#25-domain-model-and-data-flow)
   - 2.6 [API Contracts](#26-api-contracts)
   - 2.7 [Frontend Architecture](#27-frontend-architecture)
3. [Refactoring Opportunities](#3-refactoring-opportunities)
4. [Scale Considerations](#4-scale-considerations)
5. [Future Development Guide](#5-future-development-guide)
   - 5.1 [Adding a New REST Module](#51-adding-a-new-rest-module)
   - 5.2 [Test Stack Reference](#52-test-stack-reference)
   - 5.3 [Common Gotchas](#53-common-gotchas)
   - 5.4 [Security Rules That Must Not Break](#54-security-rules-that-must-not-break)

---

## 1. ADR Summary

All architecture decisions made during this engagement. ADR numbers match those recorded in `.hive/decisions.md`.

| ADR | Decision | Status | Implication |
|---|---|---|---|
| **ADR-1** | All PKs are `VARCHAR(36)` UUID strings | Stable | `@PrePersist` generates `UUID.randomUUID().toString()` in each entity; never use auto-increment integers |
| **ADR-2** | Dual-model pattern: Entity ↔ DTO; no direct entity serialisation to API | Stable | All controllers return `ApiResponse<SomeDto>` only; the entity `password` field is never leaked |
| **ADR-3** | `t_authority.url` stores permission codes (e.g. `PERM_USER_LIST`), not URL paths; enforced by Spring Security `@PreAuthorize` | Stable | The authority tree is a permission registry, not a URL ACL list |
| **ADR-4** | BCrypt replaces MD5; `password_reset_required` flag supports future migration | Stable | Legacy MD5 hashes are incompatible; migrated users need a password reset flow |
| **ADR-5** | Flyway replaces the destructive `RepairService` startup seeder | Stable | Schema lives in `backend/src/main/resources/db/migration/V*.sql`; never use `ddl-auto: create` |
| **ADR-6** | Sort fields must be whitelisted per endpoint; HTTP 400 on unknown values | Stable | Currently implemented via `@RequestParam` defaults + bean validation; extend if dynamic sort is added |
| **ADR-7** | File uploads stored outside webapp root; configurable via `APP_UPLOAD_DIR` | Stable | Default: `/var/app/uploads` (Docker volume); swap `LocalFileStorageService` for S3 without changing controllers |
| **ADR-8** | Charts returned as JSON; client-side rendering (Recharts) | Stable | No server-side image generation; `GET /users/stats/by-role` returns `[{name, count}]` |
| **ADR-9** | `TONLINE` session table dropped; online tracking via `t_user.last_activity` + 30-minute threshold | Stable | `GET /api/online` returns users with `lastActivity >= now-30m`; a scheduler clears stale values every 5 minutes |
| **ADR-10** | `BaseDaoI<T>` generic DAO pattern dropped; one Spring Data JPA repository per entity | Stable | Each repository extends `JpaRepository<Entity, String>` |
| **ADR-11** | Spring Boot 3.4.3, Java 21, JJWT 0.12.7, Lombok 1.18.44, MapStruct 1.6.3, MySQL 8, Flyway | Stable | Library versions are pinned in `backend/pom.xml`; upgrade together with compatibility matrix checks |
| **ADR-12** | `t_access_log.username` is `VARCHAR(100)` with no FK to `t_user` | Stable | Log records survive user deletion; username is a plain string, not a relationship |
| **ADR-13** (schema) | `TONLINE` table is dropped; online users derived from `t_user.last_activity` | Stable | See ADR-9 above |
| **ADR-17** | `t_role.name` values in seed data must be UPPERCASE (`ADMIN`, `GUEST`, `USER`) | Critical constraint | `UserDetailsServiceImpl` grants `"ROLE_" + name.toUpperCase()`; `@PreAuthorize("hasRole('ADMIN')")` checks `ROLE_ADMIN`; mismatch silently denies all RBAC |
| **ADR-18** | Production stack exposes only one port (80) via Nginx; backend and DB are on `backend-net` with no host ports | Stable | `resolver 127.0.0.11` in nginx.conf uses Docker DNS for runtime hostname resolution; backend restarts do not require nginx reload |

### ADR Dependency Map

```
ADR-5 (Flyway)
  └── ADR-17 (role name casing) — V3 migration corrects V2 seed data

ADR-3 (CURL → permission code)
  └── ADR-2 (DTO layer) — permission codes appear in UserDetailsServiceImpl authorities

ADR-9 (stateless JWT)
  └── ADR-18 (no shared session = safe for horizontal scaling)

ADR-4 (BCrypt)
  └── ADR-11 (Spring Security 6 BCryptPasswordEncoder)

ADR-7 (file storage)
  └── ADR-18 (uploads volume declared in docker-compose)
```

---

## 2. Architecture Overview

### 2.1 Legacy System (Reference)

The original `src/` directory is an **Oracle-backed Struts2/Spring3/Hibernate4 WAR monolith** with server-side-rendered JSP+jQuery EasyUI.

| Dimension | Legacy |
|---|---|
| MVC | Struts2 2.3.16.1 (FilterDispatcher, *.action URLs) |
| IoC / AOP | Spring 3.2.8 XML configuration |
| ORM | Hibernate 4.2.11 + OpenSessionInView filter (OSIV anti-pattern) |
| Database | Oracle 10g, SCOTT schema, uppercase column names (C-prefix convention) |
| Auth | HTTP session via `authInterceptor`; MD5 unsalted passwords; hardcoded `admin` bypass |
| DAO | Single `BaseDaoI<T>` / `BaseDaoImpl<T>` for all entities (HQL, no per-entity repository) |
| Charts | JFreeChart server-side JPEG generation (files never cleaned up) |
| Upload | Stored inside `{webapp.root}/upload/` (security risk) |
| Seeding | `RepairService.repair()` called on every startup — **destructive wipe + reseed** |
| Export | Apache POI 3.10 HSSF (old `.xls` format) |

**Key migration risks addressed:** Oracle dialect removed, MD5 replaced with BCrypt, OSIV disabled (`open-in-view: false`), HSSF upgraded to XSSF, startup wipe replaced with Flyway, admin bypass removed, uploads moved outside webapp root, HQL sort injection removed.

---

### 2.2 New System

**Stack**

| Layer | Technology | Version |
|---|---|---|
| Backend framework | Spring Boot | 3.4.3 |
| Security | Spring Security 6 + JJWT | 0.12.7 |
| ORM | Spring Data JPA + Hibernate 6 | (via Boot BOM) |
| Database | MySQL | 8.0 |
| Migrations | Flyway | (via Boot BOM) |
| Java | Java 21 (LTS) | 21 |
| Frontend framework | React + TypeScript | 18.3.1 |
| Build tool | Vite | 6.x |
| UI library | Ant Design | 5.22.2 |
| State | Zustand | 5.0.2 |
| HTTP client | Axios | 1.7.9 |
| Charts | Recharts | 3.8.x |
| Routing | React Router v6 | 6.28.0 |
| Container proxy | Nginx | 1.27-alpine |
| Build (backend) | Maven multi-stage | 3.9-eclipse-temurin-21 |
| Build (frontend) | Node multi-stage | 20-alpine |

**Artifact layout**

```
Struts2-Spring-Hibernate/        ← repository root
├── src/                         ← original Struts2 source (untouched)
├── pom.xml                      ← original Maven build (untouched)
├── backend/                     ← Spring Boot 3.4.3 rewrite
│   ├── pom.xml
│   ├── Dockerfile               ← multi-stage Maven/JRE-21 build
│   └── src/
│       ├── main/java/com/rml/system/
│       │   ├── SystemApplication.java
│       │   ├── aop/             ← AccessLogAspect
│       │   ├── config/          ← SecurityConfig, WebMvcConfig, SchedulingConfig
│       │   ├── controller/      ← 10 REST controllers
│       │   ├── dto/             ← ApiResponse, PageResponse, request/*, response/*
│       │   ├── entity/          ← 8 JPA entities + BaseEntity
│       │   ├── exception/       ← AppException, GlobalExceptionHandler
│       │   ├── repository/      ← 7 Spring Data JPA repositories
│       │   ├── security/        ← JwtTokenProvider, JwtAuthFilter, UserDetailsServiceImpl
│       │   ├── service/         ← 7 service classes
│       │   └── util/            ← FileStorageService interface + LocalFileStorageService
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── db/migration/    ← V0, V1, V2, V3
│       └── test/                ← 63 integration tests + TestDataSeeder
├── frontend/                    ← React 18 + Vite SPA
│   ├── Dockerfile               ← multi-stage Node/Nginx build
│   ├── nginx.conf               ← SPA + /api/* reverse proxy
│   └── src/
│       ├── api/                 ← client.ts (Axios), modules.ts (domain functions)
│       ├── components/          ← MainLayout, Sidebar, Header, DataTable, ModalForm
│       ├── pages/               ← 10 page components
│       ├── routes/              ← React Router config + RequireAuth guard
│       ├── store/               ← Zustand AuthStore + AppStore
│       └── types/               ← TypeScript types
├── docker-compose.yml           ← 3-container production stack
├── .env.example                 ← all env vars documented
├── .gitguardian.yml             ← test-fixture false-positive suppression
└── README.md                    ← Docker quick-start
```

---

### 2.3 System Boundaries and Component Map

```
                 ┌──────────────────────────────────────────────┐
                 │            Docker host (port 80 only)         │
                 │                                               │
  Browser  ──────►  Nginx 1.27  ◄── React SPA (static assets)  │
                 │      │                                        │
                 │      │ /api/*  (proxy_pass)                   │
                 │      ▼                                        │
                 │  Spring Boot 8080   (backend-net only)        │
                 │      │                                        │
                 │      │ JDBC / HikariCP (pool 5–20)            │
                 │      ▼                                        │
                 │  MySQL 8 3306       (backend-net only)        │
                 │      │                                        │
                 │  /var/app/uploads   (Docker volume)           │
                 └──────────────────────────────────────────────┘

External access:    port 80 only (Nginx)
Backend port:       8080 (internal, not mapped to host)
DB port:            3306 (internal, not mapped to host)
```

**Nginx responsibilities:**
- Serve React SPA static assets (Vite-hashed, 1-year cache)
- Reverse-proxy `location /api/` → `http://backend:8080` (context-path preserved)
- Rewrite `location /actuator/` → `/api/actuator/` for health probes
- SPA fallback: `try_files $uri /index.html` for client-side routing
- Security headers: `X-Frame-Options`, `X-Content-Type-Options`, `X-XSS-Protection`, `Referrer-Policy`
- `resolver 127.0.0.11` uses Docker DNS for deferred backend hostname resolution (ADR-18)

---

### 2.4 Security Architecture

**Authentication flow:**

```
POST /api/auth/login  { username, password }
    → UserService.authenticate()
        → UserRepository.findByUsername()
        → BCryptPasswordEncoder.matches()
        → User.setLastActivity(now)           [AOP: AccessLogAspect records result]
        → JwtTokenProvider.generateAccessToken(username)   [HS256, configurable TTL]
    ← { token, user: { id, username, roles } }

Subsequent requests:
    Authorization: Bearer <token>
    → JwtAuthenticationFilter.doFilterInternal()
        → JwtTokenProvider.validateToken()
        → UserDetailsServiceImpl.loadUserByUsername()
            → Grants: ROLE_<NAME> (per role) + PERM_* codes (per role's authorities)
        → SecurityContextHolder.setAuthentication()
    → Controller @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_...')")
```

**Key security classes:**

| Class | Package | Purpose |
|---|---|---|
| `SecurityConfig` | `config/` | SecurityFilterChain, CORS config, BCrypt bean |
| `JwtTokenProvider` | `security/` | Token generation + validation (JJWT 0.12.x API) |
| `JwtAuthenticationFilter` | `security/` | `OncePerRequestFilter` — populates SecurityContext |
| `JwtAuthenticationEntryPoint` | `security/` | Returns 401 JSON on unauthenticated access |
| `UserDetailsServiceImpl` | `security/` | Loads user+roles+permissions from DB; grants `ROLE_*` + `PERM_*` |
| `GlobalExceptionHandler` | `exception/` | Maps `BadCredentialsException` → 401, `AccessDeniedException` → 403 |

**Permission model:**

```
ROLE_ADMIN        → granted when t_role.name = 'ADMIN'
PERM_USER_LIST    → granted when t_authority.url = 'PERM_USER_LIST' is linked via t_role_authority

Controller usage:
  @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_USER_LIST')")
```

**CORS:** Configured in `SecurityConfig.corsConfigurationSource()`. Allowed origins read from `app.cors.allowed-origins` property (comma-separated). Default: `http://localhost`.

**Public endpoints (no JWT required):**
```
POST /api/auth/login
POST /api/auth/register
POST /api/auth/refresh
GET  /api/actuator/health
GET  /api/actuator/info
GET  /api/health
```

---

### 2.5 Domain Model and Data Flow

**Entity relationships:**

```
t_user ──< t_user_role >── t_role ──< t_role_authority >── t_authority
                                                               │
                                                         self-referential
                                                          (parent_id)

t_menu (self-referential: parent_id)
t_equipment  (standalone)
t_document   (standalone, manual_filename → /var/app/uploads/<filename>)
t_access_log (standalone, username = plain string, no FK per ADR-12)
```

**JPA entity hierarchy:**

```java
BaseEntity                               // created_at, updated_at (Spring Data Auditing)
  ├── User                               // id, username, password, lastActivity, passwordResetRequired
  │     └── Set<Role> roles              // @ManyToMany LAZY via t_user_role
  ├── Role                               // id, name, description
  │     └── Set<Authority> authorities   // @ManyToMany LAZY via t_role_authority
  ├── Authority                          // id, name, url (permission code), sequence
  │     ├── Authority parent             // @ManyToOne LAZY (nullable = root node)
  │     └── List<Authority> children     // @OneToMany LAZY, ordered by sequence
  ├── Menu                               // id, name, iconClass, url (route path), sequence
  │     ├── Menu parent                  // @ManyToOne LAZY
  │     └── List<Menu> children          // @OneToMany LAZY, ordered by sequence
  ├── Equipment                          // id, model, name, producer, description, quantity
  ├── Document                           // id, model, name, producer, quantity, manualFilename
  └── AccessLog                          // id, username, ip, accessedAt, message
```

**Transaction boundaries:** All service methods are `@Transactional` (write) or `@Transactional(readOnly = true)` (read). JPA `open-in-view` is **disabled** — lazy collections must be initialised within the service transaction.

**Flyway migration state:**

| Version | Description | Applied |
|---|---|---|
| V0 | Baseline (empty) — for fresh installs | Yes |
| V1 | Full 9-table schema (all DDL) | Yes |
| V2 | Seed data: admin user, 3 roles, authority tree, menu tree, user-role mapping | Yes |
| V3 | Fix role name: `'Administrator'` → `'ADMIN'` (ADR-17) | Yes |

Next migration must be `V4__description.sql`.

**Note on V2/V3 technical debt:** V2 seeds role name `'Administrator'`; V3 immediately corrects this to `'ADMIN'`. If you ever need to recreate migrations from scratch, V2 should seed `'ADMIN'` directly to avoid this two-migration dance. This is frozen history now — do not modify applied migration checksums.

---

### 2.6 API Contracts

All responses use the `ApiResponse<T>` envelope:

```json
{
  "success": true,
  "message": "OK",
  "code": 200,
  "data": { ... }
}
```

Paginated responses return a Spring `Page<T>` as `data.content` / `data.totalElements` / `data.pageable`.

**Endpoint inventory:**

| Controller | Path | Methods | Auth |
|---|---|---|---|
| `AuthController` | `/auth/login`, `/auth/logout`, `/auth/register`, `/auth/refresh` | POST | Public |
| `UserController` | `/users` | GET(page), POST | `PERM_USER_LIST` / `PERM_USER_ADD` |
| `UserController` | `/users/{id}` | PUT | `PERM_USER_EDIT` |
| `UserController` | `/users` (batch) | DELETE `?ids=a,b` | `PERM_USER_DELETE` |
| `UserController` | `/users/roles` | PUT | `PERM_USER_ROLE_EDIT` |
| `UserController` | `/users/stats/by-role` | GET | `PERM_USER_LIST` |
| `UserController` | `/users/all` | GET | `PERM_USER_LIST` |
| `RoleController` | `/roles` | GET(page), POST | `PERM_ROLE_LIST` / `PERM_ROLE_ADD` |
| `RoleController` | `/roles/{id}` | PUT, DELETE | `PERM_ROLE_EDIT` / `PERM_ROLE_DELETE` |
| `RoleController` | `/roles/{id}/authorities` | PUT | `PERM_ROLE_EDIT` |
| `RoleController` | `/roles/all` | GET | `PERM_ROLE_LIST` |
| `AuthorityController` | `/authorities/tree` | GET | Authenticated |
| `AuthorityController` | `/authorities` | GET(flat), POST | Authenticated |
| `AuthorityController` | `/authorities/{id}` | PUT, DELETE | Authenticated |
| `MenuController` | `/menus/tree` | GET | Authenticated |
| `EquipmentController` | `/equipment` | GET(page), POST | `PERM_EQUIP_LIST` / `PERM_EQUIP_ADD` |
| `EquipmentController` | `/equipment/{id}` | PUT, DELETE | `PERM_EQUIP_EDIT` / `PERM_EQUIP_DELETE` |
| `EquipmentController` | `/equipment/export` | GET (xlsx blob) | `PERM_EQUIP_LIST` |
| `DocumentController` | `/documents` | GET(page), POST | `PERM_DOC_LIST` / `PERM_DOC_ADD` |
| `DocumentController` | `/documents/{id}` | PUT, DELETE | `PERM_DOC_EDIT` / `PERM_DOC_DELETE` |
| `DocumentController` | `/documents/{id}/manual` | POST (upload) | `PERM_DOC_UPLOAD` |
| `DocumentController` | `/documents/manual/{filename}` | GET (download) | Authenticated |
| `LogController` | `/logs` | GET(page, `?name=`) | Authenticated |
| `OnlineController` | `/online` | GET | Authenticated |
| `HealthController` | `/health` | GET | Public |

**Pagination query parameters:** All paginated endpoints accept `?page=0&size=10&name=<filter>`.

**Batch delete pattern:** `DELETE /users?ids=id1,id2,id3` — comma-separated ID list as a single query param.

**File upload:** `POST /documents/{id}/manual` — `multipart/form-data`, field name `file`. Max 100 MB (Spring) / 100 MB (Nginx).

**Excel export:** `GET /equipment/export` — returns `application/octet-stream` blob with `Content-Disposition: attachment; filename="equipment.xlsx"`.

---

### 2.7 Frontend Architecture

**State management (Zustand):**

```typescript
// AuthStore (persisted to localStorage as 'lab-auth')
{ token, user, isAuthenticated, setAuth(), clearAuth() }

// AppStore (session memory only)
{ sidebarCollapsed, openTabs, activeTabKey, openTab(), closeTab(), ... }
```

**Routing (React Router v6):**

```
/ → <Navigate to="/dashboard">
/login → <Login> (public, redirects to /dashboard if already authed)
/* (protected, wrapped in RequireAuth + MainLayout):
  /dashboard → Dashboard
  /users → UserManagement
  /roles → RoleManagement
  /authorities → AuthorityManagement
  /equipment → EquipManagement
  /documents → DocManagement
  /logs → LogViewer
  /stats/users → UserStats
  /online-users → OnlineUsers
* → <Navigate to="/dashboard">
```

All pages are lazy-loaded (`React.lazy` + `Suspense`).

**API client (`src/api/client.ts`):**
- Base URL: `VITE_API_BASE_URL` env var (defaults to `/api`); in dev, proxied by Vite to `localhost:8080`; in production, Nginx proxies
- Request interceptor: attaches `Authorization: Bearer <token>` from AuthStore
- Response interceptor: `401` → `clearAuth()` + redirect `/login`; `403` → antd `message.error`; `5xx` → antd `message.error`

**API modules (`src/api/modules.ts`):** All REST calls organised by domain. Domain functions typed with TypeScript interfaces from `src/types/index.ts`.

**Layout shell (`MainLayout`):** Ant Design `Layout` with `Sider` (Sidebar), `Header`, and `Content`. Sidebar loads menu tree dynamically from `GET /api/menus/tree` on mount; falls back to a static hardcoded menu if the API fails.

**Tab navigation:** `AppStore.openTabs` tracks open pages as closeable tabs. The home tab (`/dashboard`) cannot be closed.

---

## 3. Refactoring Opportunities

The following issues exist in the current codebase. They are not blocking but should be addressed in the next iteration.

### 3.1 DTO Mapping — Migrate to MapStruct (Medium)

**Problem:** All `toDto()` methods are hand-written in service classes (e.g., `UserService.toDto()`, `RoleService.toDto()`). This is ~300 lines of boilerplate that will grow with every new field.

**Evidence:** MapStruct 1.6.3 is already declared as a dependency and annotation processor in `pom.xml` but no `@Mapper` interfaces exist.

**Fix:** Create MapStruct mapper interfaces in a `mapper/` package:
```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(CreateUserRequest req);
}
```
Remove the hand-written `toDto()` methods from service classes.

---

### 3.2 N+1 Query in getRoleStats() (High — Scales Poorly)

**Problem:** `UserService.getRoleStats()` loads ALL users into memory, then iterates roles in Java:
```java
List<User> users = userRepository.findAll();   // O(N) DB fetch
for (User user : users) {
    for (Role role : user.getRoles()) { ... }  // N * M lazy loads
}
```
This is O(N×M) in user×role count and will degrade significantly at scale.

**Fix:** Replace with a single aggregate query:
```java
@Query("SELECT r.name, COUNT(u.id) FROM User u JOIN u.roles r GROUP BY r.name")
List<Object[]> countUsersByRole();
```
Or via a native query:
```sql
SELECT r.name, COUNT(ur.user_id)
FROM t_role r JOIN t_user_role ur ON r.id = ur.role_id
GROUP BY r.name
```

---

### 3.3 Bulk UPDATE in SchedulingConfig (High — Scales Poorly)

**Problem:** `clearInactiveUsers()` loads all users, filters in Java, saves individually — an O(N) read + O(K) writes per 5-minute tick:
```java
userRepository.findAll().stream()
    .filter(u -> u.getLastActivity() != null && u.getLastActivity().isBefore(threshold))
    .forEach(u -> { u.setLastActivity(null); userRepository.save(u); });
```

**Fix:** Add a custom repository method for a single bulk UPDATE:
```java
@Modifying
@Transactional
@Query("UPDATE User u SET u.lastActivity = null WHERE u.lastActivity < :threshold")
int clearActivityBefore(@Param("threshold") LocalDateTime threshold);
```

---

### 3.4 No Service Interfaces (Low — Testability)

**Problem:** Services are concrete classes (`UserService`, `RoleService`, etc.), not interface+implementation pairs. The `FileStorageService` correctly uses an interface, but no business services do. This makes mocking harder and prevents compile-time contract enforcement.

**Fix:** Extract `UserService`, `RoleService`, `AuthorityService`, etc. into interfaces and rename concrete classes to `UserServiceImpl`, `RoleServiceImpl`, etc. — a standard Spring idiom.

---

### 3.5 AccessLogAspect Scope Too Narrow (Low)

**Problem:** `AccessLogAspect` only intercepts `UserService.authenticate()`. The original system logged more significant operations. Other write operations (user create/delete, role changes) are not audited.

**Fix:** Extend the pointcut to cover significant CRUD operations, or use a custom annotation (`@Audited`) to mark methods to log:
```java
@Pointcut("@annotation(com.rml.system.aop.Audited)")
public void auditedOperation() {}
```

---

### 3.6 No Caching on Tree Endpoints (Low — Scales Poorly)

**Problem:** `GET /api/menus/tree` and `GET /api/authorities/tree` hit the DB on every sidebar load. These change rarely but are fetched on every authenticated page load.

**Fix:** Add Spring Cache (backed by Caffeine):
```java
@Cacheable("menuTree")
public List<MenuDto> getMenuTree() { ... }

@CacheEvict(value = "menuTree", allEntries = true)
public MenuDto createMenu(...) { ... }
```
Add `spring-boot-starter-cache` and Caffeine dependency.

---

### 3.7 No Startup Validation of JWT Secret (Medium — Security)

**Problem:** `application.yml` ships with a placeholder JWT secret (`your-256-bit-secret-key-replace-in-production...`). There is no startup check that rejects this placeholder.

**Fix:** Add a `@PostConstruct` validation in `JwtTokenProvider`:
```java
@PostConstruct
void validateSecret() {
    if (secret.contains("replace-in-production")) {
        throw new IllegalStateException(
            "JWT secret is the default placeholder — rotate before deployment");
    }
}
```

---

### 3.8 V2 Seed Inconsistency (Low — Documentation Debt)

**Problem:** V2 seeds role name `'Administrator'`; V3 immediately corrects it to `'ADMIN'`. Flyway checksums are frozen so V2 cannot be altered. But any developer reading V2 in isolation sees an incorrect seed.

**Fix:** Add a comment at the top of V2__seed.sql:
```sql
-- NOTE: The 'Administrator' name in the INSERT below is intentionally incorrect.
-- V3__fix_role_name.sql renames it to 'ADMIN' per ADR-17.
-- Do not attempt to change this value here as Flyway checksums are frozen.
```

---

### 3.9 Stale lastActivity on Login Update Pattern (Low)

**Problem:** `lastActivity` is only updated on `UserService.authenticate()` (login). If a user is active for hours between logins, their activity timestamp becomes stale and the online users list will show them as offline even though they are actively using the system.

**Fix:** Update `lastActivity` on every authenticated request by hooking `JwtAuthenticationFilter` or adding a `HandlerInterceptor`:
```java
// In JwtAuthenticationFilter, after populating SecurityContext:
String username = jwtTokenProvider.getSubject(token);
userRepository.updateLastActivity(username, LocalDateTime.now());
```
This requires a `@Modifying` bulk UPDATE method to avoid the overhead of a full entity load+save.

---

### 3.10 Manual `toDto()` in Controller-level Page mapping (Low)

**Problem:** Some services return `Page<Entity>` mapped via `.map(this::toDto)` inside the service. This is correct, but because there are no service interfaces, changing the `toDto()` signature is a refactor across service + test class. MapStruct (3.1 above) resolves this.

---

## 4. Scale Considerations

The current architecture is a correctly-designed single-node system. The following analysis addresses what changes when load or team size grows.

### 4.1 Stateless Backend — Horizontal Scaling Ready

The JWT stateless auth (ADR-9) means **backend containers can be replicated behind a load balancer with no shared session state**. The only shared state is:
- MySQL (single write endpoint)
- `/var/app/uploads` Docker volume (single filesystem — see §4.4)

To scale: run multiple `backend` containers behind Nginx or a load balancer. JWT validation is self-contained in each replica.

### 4.2 Database Scaling Ceiling

Current MySQL 8 setup:
- Single instance, no read replicas
- HikariCP pool: 5–20 connections per backend replica
- No query caching

**Write bottleneck:** All CRUD operations go to a single MySQL instance. Acceptable for hundreds of concurrent users; needs attention above ~1000 req/sec.

**Read bottleneck:** `getRoleStats()` and `clearInactiveUsers()` perform full table scans (see §3.2, §3.3 for fixes). After those are fixed, add a read replica for read-only `UserService.listUsers()`, `listAll()` etc.

**Scaling path:**
1. Fix N+1 queries (§3.2, §3.3) — immediate win
2. Add MySQL read replica + route `@Transactional(readOnly = true)` to it
3. Add Caffeine cache for tree endpoints (§3.6)
4. Consider PgBouncer or ProxySQL connection pooling before multiple backend replicas share the same DB

### 4.3 Access Log Growth

`t_access_log` has no TTL or partitioning. Currently indexed on `username` and `accessed_at`. At ~100 logins/day: ~36,000 rows/year — manageable. At ~10,000 logins/day: ~3.6M rows/year — queries slow without maintenance.

**Mitigation:**
- Add a scheduled job to archive or delete rows older than N days
- Or use MySQL table partitioning by `accessed_at` month
- The existing `idx_log_accessed_at` index supports range queries already

### 4.4 File Upload Storage

`LocalFileStorageService` stores files on a Docker volume. In a **multi-replica backend** scenario, each replica would need access to the same volume. A local Docker volume is not shared across hosts.

**Scaling path:**
- Short term: mount the uploads volume via NFS on all backend replicas
- Long term: implement an `S3FileStorageService` implementing `FileStorageService` interface (ADR-7 was designed for this swap) and wire it instead of `LocalFileStorageService`

### 4.5 Rate Limiting and DDoS

No rate limiting is present. `POST /api/auth/login` is unprotected against brute force.

**Add before internet-facing deployment:**
- Nginx `limit_req_zone` on `/api/auth/login`
- Or Spring Security `BruteForceProtection` / custom login attempt tracking

### 4.6 No HTTPS

Nginx is configured for HTTP (port 80) only. All JWT tokens, passwords, and session data transit in plaintext.

**Must add before internet-facing deployment:** Nginx TLS termination (Let's Encrypt / cert-manager) or a TLS-terminating load balancer in front.

### 4.7 Token Revocation Gap

Stateless JWT means a stolen token is valid until expiry (24 hours by default). There is no server-side denylist.

**Mitigations (increasing complexity):**
1. Reduce `JWT_EXPIRATION_MS` to 1–4 hours for sensitive deployments
2. Implement a Redis-backed token denylist checked in `JwtAuthenticationFilter`
3. Use short-lived access tokens + refresh token rotation

### 4.8 Docker Resource Limits Not Set

`docker-compose.yml` does not set `deploy.resources.limits`. An OOM or runaway query can consume all host memory.

**Add for production:**
```yaml
backend:
  deploy:
    resources:
      limits:
        memory: 768m
        cpus: '1.0'
```

---

## 5. Future Development Guide

### 5.1 Adding a New REST Module

Follow this checklist for any new domain entity:

**1. Database migration (`V4__add_<entity>.sql`):**
```sql
CREATE TABLE t_new_entity (
    id          VARCHAR(36)  NOT NULL,
    -- columns...
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**2. JPA Entity (`entity/NewEntity.java`):**
```java
@Entity @Table(name = "t_new_entity")
public class NewEntity extends BaseEntity {
    @Id @Column(name = "id", length = 36) private String id;
    @PrePersist public void prePersist() { if (id == null) id = UUID.randomUUID().toString(); }
    // fields with @Column
}
```

**3. Repository (`repository/NewEntityRepository.java`):**
```java
public interface NewEntityRepository extends JpaRepository<NewEntity, String> {
    Page<NewEntity> findByNameContainingIgnoreCase(String q, Pageable pageable);
}
```

**4. DTOs** in `dto/request/CreateNewEntityRequest.java` and `dto/response/NewEntityDto.java`

**5. Service (`service/NewEntityService.java`):**
- `@Service @RequiredArgsConstructor @Transactional`
- `listPage(String q, Pageable pageable)`
- `create(CreateNewEntityRequest req)`
- `update(String id, UpdateNewEntityRequest req)`
- `delete(List<String> ids)`
- `toDto(NewEntity entity)` — or use MapStruct (§3.1)

**6. Controller (`controller/NewEntityController.java`):**
- `@RestController @RequestMapping("/new-entities") @RequiredArgsConstructor`
- `@PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_NEWENTITY_LIST')")` on each method
- Return `ApiResponse<Page<NewEntityDto>>` for list, `ApiResponse<NewEntityDto>` for create/update
- Batch delete: `@DeleteMapping` + `@RequestParam String ids` (comma-split)

**7. Permission codes in V5 migration:** Add `t_authority` rows with permission codes `PERM_NEWENTITY_LIST`, `PERM_NEWENTITY_ADD`, etc. and link to ADMIN role.

**8. Menu entry in V5 migration:** Add `t_menu` row pointing to the new React route.

**9. Frontend types** (`src/types/index.ts`): Add `NewEntityDto`, `NewEntityCreateRequest` interfaces.

**10. Frontend API** (`src/api/modules.ts`): Add `newEntityApi` object with `list`, `create`, `update`, `delete` functions.

**11. Frontend page** (`src/pages/NewEntityManagement.tsx`): Follow the pattern in `EquipManagement.tsx` — DataTable + ModalForm.

**12. Route** (`src/routes/index.tsx`): Add lazy import + `{ path: '/new-entities', element: <S><NewEntityManagement /></S> }`.

---

### 5.2 Test Stack Reference

**Backend tests (`backend/src/test/`):**

| Test Class | Count | What it tests |
|---|---|---|
| `SystemApplicationTests` | 1 | Application context loads |
| `HealthControllerIntegrationTest` | ~5 | Health endpoint responds |
| `JwtTokenProviderTest` | ~8 | Token generation, expiry, validation |
| `SecurityFilterChainIntegrationTest` | ~10 | Public vs protected endpoint access |
| `FeatureApiContractIntegrationTest` | ~32 | Full HTTP round-trip for all 10 modules including search filters |
| `PmVerificationScenariosTest` | 7 | PM acceptance scenarios (login→CRUD end-to-end) |
| `TestDataSeeder` | (fixture) | Inserts test data into H2 before test runs |
| **Total** | **63** | All pass on H2 in-memory DB with `test` profile |

**Test profile:**
- `src/test/resources/application-test.yml` — H2 in-memory DB, Flyway enabled with H2 dialect
- Credentials injected via `@Value("${test.admin.username}")` etc. (never literal strings in source)
- Run: `cd backend && mvn test -Dspring.profiles.active=test`

**Frontend tests (`frontend/src/`):**

| Test File | Count | What it tests |
|---|---|---|
| `__tests__/Login.test.tsx` | 5 | Login form render + submit |
| `__tests__/authStore.test.ts` | 5 | Zustand AuthStore setAuth/clearAuth |
| `__tests__/apiModules.test.ts` | 5 | API module function signatures |
| **Total** | **15** | Vitest + @testing-library/react |

- Run: `cd frontend && npm test`

**Known test issue (low priority):** `DocumentUploadOverwrite` test in `FeatureApiContractIntegrationTest` is intermittently flaky when `/tmp/test-uploads` has stale state from a previous test run. Add `@BeforeEach` directory cleanup to fix.

---

### 5.3 Common Gotchas

**1. Role names must be UPPERCASE in the database (ADR-17)**

`UserDetailsServiceImpl` constructs `"ROLE_" + role.getName().toUpperCase()`. Controllers use `hasRole('ADMIN')`. Spring Security maps `hasRole('ADMIN')` → checks for `ROLE_ADMIN`.

If a new role is seeded with `name = 'manager'`, `hasRole('MANAGER')` checks `ROLE_MANAGER` which is granted as `ROLE_MANAGER` (toUpperCase) — this works. But if you use `hasRole('Manager')` it checks `ROLE_Manager` which never matches. Always use UPPERCASE role names and UPPERCASE in `hasRole()`.

**2. Search param is `?name=`, not `?q=` (MISMATCH-1, resolved in t27)**

All four list endpoints accept `?name=` as the filter parameter. The controllers bind it as:
```java
@RequestParam(name = "name", required = false) String q
```
The internal variable is named `q` but the HTTP parameter is `name`. Do not add `?q=` filtering; extend via the existing `name` binding convention.

**3. Lazy loading after transaction close**

`open-in-view: false` is set. If you access `user.getRoles()` outside a `@Transactional` boundary, you get `LazyInitializationException`. Always fetch necessary associations within the service method's transaction, or use a JPQL `JOIN FETCH` query.

**4. JWT secret length**

JJWT 0.12.x requires the secret key to be at least 256 bits (32 bytes) for HS256. The placeholder in `application.yml` meets this requirement but the value must be replaced before deployment. Keys shorter than 32 chars will throw on startup.

**5. CORS origins must match exactly**

`app.cors.allowed-origins` is a comma-separated list used as exact string matches. Include protocol and port: `http://localhost:3000` is different from `http://localhost`. In production Docker Compose, the default is `http://localhost` (port 80 only).

**6. Flyway checksum immutability**

Never edit an applied migration file — Flyway will refuse to start with a checksum error. Always create a new `V(N+1)__description.sql` to evolve the schema.

**7. V0__baseline.sql**

Exists for environments where the DB already has a schema before Flyway is introduced (Flyway `baseline-on-migrate: true`). It is an empty file and does nothing on a fresh install.

**8. JAVA_OPTS in Docker Compose**

Default heap: `-Xmx512m -Xms256m`. If the Spring Boot application crashes with `OutOfMemoryError`, increase via `JAVA_OPTS=-Xmx1g -Xms512m` in `.env`. Do not exceed ~75% of the container's memory limit.

**9. File upload path traversal protection**

`LocalFileStorageService.store()` normalises the path and checks `dest.startsWith(basePath)`. Never bypass this check. Any custom `FileStorageService` implementation must replicate this guard.

**10. Actuator health endpoint location**

The backend exposes `/api/actuator/health` (context-path `/api`). The Nginx `actuator` location block rewrites `/actuator/*` → `/api/actuator/*`. Docker Compose healthcheck calls `http://localhost:8080/api/actuator/health` (directly to backend, no Nginx). These two paths must both remain functional.

---

### 5.4 Security Rules That Must Not Break

These constraints are architectural invariants. Violating any one of them can introduce a security vulnerability.

| Rule | Rationale | Where enforced |
|---|---|---|
| **Never serialise entities directly to API responses** (ADR-2) | Exposes `password` hash, internal IDs, lazy-loaded associations | `ApiResponse<T>` where `T` is always a DTO |
| **All non-public endpoints require `@PreAuthorize`** (ADR-3) | Prevents privilege escalation if new endpoints are added | `SecurityConfig` catches anything not `@PreAuthorize`'d at `anyRequest().authenticated()` level |
| **Sort field whitelisting** (ADR-6) | SQL injection via unsorted `ORDER BY` values | Currently `@RequestParam(defaultValue=...)` + bean validation; if dynamic sort is added, maintain a whitelist Set |
| **Path-check all uploaded filenames** | Path traversal to arbitrary filesystem writes | `LocalFileStorageService.store()`: `dest.normalize()` + `startsWith(basePath)` |
| **JWT secret must be rotated before internet-facing deployment** | Default placeholder is in source history | `.env.example` documents this; add startup validation (§3.7) |
| **Role names in DB must be UPPERCASE** (ADR-17) | RBAC silently breaks on case mismatch | `UserDetailsServiceImpl`, V3 migration |
| **BCrypt only — never MD5** (ADR-4) | Salted BCrypt; MD5 is reversible with rainbow tables | `SecurityConfig.passwordEncoder()` returns `BCryptPasswordEncoder` |
| **Flyway `ddl-auto: validate` only** (ADR-5) | `create` or `create-drop` would wipe production data | `application.yml`: `hibernate.ddl-auto: validate` |
| **No entity FKs on access log username** (ADR-12) | Preserves audit trail when users are deleted | `t_access_log.username VARCHAR(100)` — no FK constraint |
| **Content-Security-Policy** | Currently only `X-Frame-Options` etc. set in Nginx | Add `Content-Security-Policy` header to Nginx config before production |

---

## Appendix: Library Version Compatibility Matrix

| Library | Version | Notes for upgrade |
|---|---|---|
| Spring Boot | 3.4.3 | Next: 3.5.x (non-breaking); 4.x requires Java 17+ baseline |
| JJWT | 0.12.7 | 0.12.x is stable GA; 1.x not yet released at time of writing |
| Lombok | 1.18.44 | Java 25 compatible; check release notes when upgrading Java |
| MapStruct | 1.6.3 | Annotation processor order: Lombok must precede MapStruct in `pom.xml` (`lombok-mapstruct-binding` 0.2.0 required) |
| Flyway | (Boot BOM) | MySQL-specific support requires `flyway-mysql` artifact (already in pom.xml) |
| React | 18.3.1 | React 19 is backward-compatible but changes `ReactDOM.render` API |
| Ant Design | 5.22.2 | 5.x is the current major; check migration guide for 6.x when available |
| Vite | 6.0.3 | Node 18+ required; Node 20 is the Docker build base |
| Recharts | 3.8.x | SVG-based, no canvas fallback; bundle size ~300KB gzipped |

---

*Prepared by Architect Agent | Task t54 | 2026-03-13*
