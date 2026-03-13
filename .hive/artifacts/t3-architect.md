# Target Architecture Design: Spring Boot 3 + React 19
**Task:** t3 | **Role:** Architect | **Status:** Complete

---

## [scope] Design-only document artifact (files: 1 doc, subsystems: 2 — backend API + frontend SPA)

---

## 1. Target Technology Stack

| Layer | Technology | Version | Replaces |
|---|---|---|---|
| Backend Framework | Spring Boot | 3.5.x | Struts2 2.3.x + Spring 3.2.x |
| Security | Spring Security 6 + JWT (nimbus-jose-jwt) | 6.3.x | Custom AuthInterceptor + MD5 session auth |
| ORM | Spring Data JPA + Hibernate 6 | 6.x | Hibernate 4 + generic BaseDaoImpl |
| Database | PostgreSQL 16 (or MySQL 8) | 16.x / 8.x | Oracle 10g (SCOTT schema) |
| DB Migration | Flyway | 10.x | RepairServiceImpl startup seeder |
| File Storage | Local FS (configurable) or S3-compatible | — | `{webapp.root}/upload/` |
| Excel Export | Apache POI (XSSF) | 5.x | Apache POI (HSSF) 3.10 |
| Build | Maven (JAR, not WAR) | 3.9.x | Maven WAR |
| Java | Java 21 (LTS) | 21 | Java 8 |
| Frontend Framework | React 19 + TypeScript | 19.x | jQuery EasyUI 1.3.1 |
| Frontend Build | Vite | 6.x | None (JSP) |
| State Management | Zustand | 5.x | jQuery global state |
| HTTP Client | Axios | 1.x | jQuery.ajax |
| UI Component Library | Ant Design 5 (antd) | 5.x | jQuery EasyUI |
| Routing | React Router v7 | 7.x | Struts2 action URL routing |
| Charts | Recharts | 2.x | JFreeChart (server-side JPEG) |
| Logging | SLF4J + Logback | 2.x | Log4j 1.2 |

**Rationale for PostgreSQL:** Oracle 10g is not available in Maven Central (ojdbc14 requires local install), PostgreSQL is open-source, has first-class Spring Boot/Flyway support, and uses standard ANSI SQL. MySQL 8 is an acceptable alternative — the schema design below uses standard types compatible with both.

**Rationale for Ant Design:** Provides DataTable, Tree, TreeSelect, Form, Modal, and Chart-adjacent components that are direct conceptual replacements for EasyUI's datagrid, tree, dialog, and form components. This minimizes frontend re-design scope.

---

## 2. Repository Layout

The rewrite lives in a **new sibling directory** alongside the original source.

```
struts2-easyui-system/          ← original (read-only reference)
easyui-rewrite/                 ← NEW root
  backend/                      ← Spring Boot Maven project
    pom.xml
    src/
      main/
        java/com/rml/system/
          SystemApplication.java          ← @SpringBootApplication entry point
          config/
            SecurityConfig.java           ← SecurityFilterChain, JWT decoder
            WebConfig.java                ← CORS, static resources
            JwtProperties.java            ← @ConfigurationProperties for JWT params
            FileStorageProperties.java    ← upload path config
          controller/
            AuthController.java           ← POST /api/auth/login, POST /api/auth/logout
            UserController.java
            RoleController.java
            AuthorityController.java
            MenuController.java
            EquipmentController.java
            DocumentController.java
            LogController.java
            OnlineController.java
          service/
            UserService.java
            RoleService.java
            AuthorityService.java
            MenuService.java
            EquipmentService.java
            DocumentService.java
            LogService.java
            OnlineService.java
          service/impl/
            UserServiceImpl.java
            ... (one per service)
          repository/
            UserRepository.java
            RoleRepository.java
            AuthorityRepository.java
            MenuRepository.java
            UserRoleRepository.java
            RoleAuthorityRepository.java
            EquipmentRepository.java
            DocumentRepository.java
            LogRepository.java
            OnlineRepository.java
          entity/
            User.java
            Role.java
            Authority.java
            Menu.java
            UserRole.java
            RoleAuthority.java
            Equipment.java
            Document.java
            AccessLog.java
            OnlineUser.java
          dto/
            request/
              LoginRequest.java
              CreateUserRequest.java
              UpdateUserRequest.java
              BatchRoleRequest.java
              CreateRoleRequest.java
              UpdateRoleRequest.java
              CreateAuthorityRequest.java
              UpdateAuthorityRequest.java
              CreateEquipmentRequest.java
              UpdateEquipmentRequest.java
              CreateDocumentRequest.java
              UpdateDocumentRequest.java
            response/
              ApiResponse.java             ← generic {success, msg, data}
              PageResponse.java            ← generic {total, rows}
              LoginResponse.java           ← {token, username, authorities}
              UserDto.java
              RoleDto.java
              AuthorityDto.java
              AuthorityTreeNode.java
              MenuTreeNode.java
              EquipmentDto.java
              DocumentDto.java
              LogDto.java
              OnlineUserDto.java
              RoleChartDto.java            ← {name, count} for chart
          aop/
            AccessLogAspect.java           ← replaces LogServiceImpl @Aspect
          exception/
            GlobalExceptionHandler.java    ← @RestControllerAdvice
            BusinessException.java
          util/
            IpUtil.java
            SortFieldValidator.java
            FileStorageService.java
        resources/
          application.yml
          application-dev.yml
          application-prod.yml
          db/migration/
            V1__schema.sql                 ← DDL: all tables
            V2__seed.sql                   ← Admin user, default roles, menus, authorities
            V3__seed_equipment.sql         ← Optional: demo equipment & docs
      test/
        java/com/rml/system/
          controller/  (integration tests)
          service/     (unit tests)
  frontend/                      ← React 19 Vite project
    package.json
    vite.config.ts
    tsconfig.json
    index.html
    src/
      main.tsx
      App.tsx
      router/
        index.tsx                  ← React Router v7 route definitions
        ProtectedRoute.tsx
      store/
        authStore.ts               ← Zustand: JWT token, current user, authorities
        onlineStore.ts
      api/
        client.ts                  ← Axios instance with JWT interceptor
        auth.ts
        users.ts
        roles.ts
        authorities.ts
        menus.ts
        equipment.ts
        documents.ts
        logs.ts
        online.ts
      pages/
        Login.tsx
        Layout.tsx                 ← Main shell: Sider + Header + Content tabs
        users/
          UserList.tsx
          UserForm.tsx
          BatchRoleForm.tsx
        roles/
          RoleList.tsx
          RoleForm.tsx
        authorities/
          AuthorityTree.tsx
          AuthorityForm.tsx
        equipment/
          EquipmentList.tsx
          EquipmentForm.tsx
        documents/
          DocumentList.tsx
          DocumentForm.tsx
          ManualUploadForm.tsx
        logs/
          LogList.tsx
        chart/
          UserRoleChart.tsx
      components/
        PagedTable.tsx             ← Generic antd Table with server-side pagination
        TreeSelectField.tsx
        ConfirmDeleteButton.tsx
        OnlinePanel.tsx
      types/
        api.ts                     ← TypeScript interfaces matching all DTOs
        index.ts
      hooks/
        usePagedQuery.ts
        useAuthorities.ts
      utils/
        formatters.ts
```

---

## 3. API Contracts

### 3.1 Base Conventions

- **Base path:** `/api/v1`
- **Content-Type:** `application/json` for all request/response bodies
- **Authentication:** `Authorization: Bearer <JWT>` header on all protected endpoints
- **Pagination params:** `page` (1-based), `size` (default 10), `sort` (field name), `order` (`asc`|`desc`)
- **Sort safety:** Each controller whitelists allowed sort fields; unknown values → HTTP 400
- **Response envelope:**
  ```json
  // Single resource / action response
  { "success": true, "msg": "OK", "data": { ... } }

  // Paginated list response
  { "total": 100, "rows": [ ... ] }
  ```
- **Error response:**
  ```json
  { "success": false, "msg": "Human-readable error", "code": "ERROR_CODE" }
  ```
- **HTTP status codes:**
  - `200 OK` — success
  - `201 Created` — resource created
  - `400 Bad Request` — validation failure, unknown sort field
  - `401 Unauthorized` — missing or invalid JWT
  - `403 Forbidden` — authenticated but insufficient authority
  - `404 Not Found` — resource not found
  - `409 Conflict` — duplicate key (username exists)
  - `500 Internal Server Error` — unhandled exception

### 3.2 Authentication Endpoints (no JWT required)

#### `POST /api/auth/login`
Request:
```json
{ "username": "admin", "password": "admin" }
```
Response `200`:
```json
{
  "success": true,
  "msg": "Login successful",
  "data": {
    "token": "<JWT>",
    "username": "admin",
    "authorities": ["PERM_USER_LIST", "PERM_USER_ADD", ...]
  }
}
```
Response `401`:
```json
{ "success": false, "msg": "Invalid username or password" }
```

**Notes:**
- JWT contains claims: `sub` (userId), `username`, `authorities` (array of permission codes), `iat`, `exp`
- Token expiry: 2 hours (configurable via `jwt.expiration-ms`)
- Refresh token strategy: separate `POST /api/auth/refresh` endpoint with longer-lived refresh token (7 days)
- Login attempt is recorded to access log via AOP regardless of success/failure

#### `POST /api/auth/refresh`
Request header: `Authorization: Bearer <refresh-token>`
Response `200`: `{ "data": { "token": "<new-access-JWT>" } }`

#### `POST /api/auth/logout`
(Stateless — client discards token. Server optionally records logout event.)
Response `200`: `{ "success": true }`

#### `POST /api/auth/register`
Request:
```json
{ "username": "newuser", "password": "secret" }
```
Response `201`: `{ "success": true, "msg": "User registered" }`
Response `409`: `{ "success": false, "msg": "Username already exists" }`
- Username `admin` is rejected with `409`

---

### 3.3 User Endpoints (`/api/users`)

All require `PERM_USER_*` authority or admin role.

| Method | Path | Permission | Description |
|---|---|---|---|
| GET | `/api/users` | `PERM_USER_LIST` | Paginated list with optional `q` search |
| POST | `/api/users` | `PERM_USER_ADD` | Create user |
| PUT | `/api/users/{id}` | `PERM_USER_EDIT` | Update user |
| DELETE | `/api/users` | `PERM_USER_DELETE` | Batch delete (body: `{"ids": [...]}`) |
| PUT | `/api/users/roles` | `PERM_USER_ROLE_EDIT` | Batch role assignment |
| GET | `/api/users/role-chart` | `PERM_USER_LIST` | Role distribution data for chart |
| GET | `/api/users/combobox` | (public or any-auth) | Id+name list for select inputs |

**GET /api/users** — Query params: `page`, `size`, `sort` (allowed: `username`, `createdAt`), `order`, `q`

Response:
```json
{
  "total": 5,
  "rows": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "alice",
      "password": "******",
      "createdAt": "2024-01-15T10:30:00Z",
      "modifiedAt": "2024-03-01T09:00:00Z",
      "roles": [{ "id": "...", "name": "Admin" }]
    }
  ]
}
```

**POST /api/users** — Request:
```json
{ "username": "alice", "password": "plain-text", "roleIds": ["role-uuid-1"] }
```
Response `201`: `{ "success": true, "data": { "id": "new-uuid" } }`

**PUT /api/users/{id}** — Request:
```json
{ "username": "alice2", "password": "newpass", "roleIds": ["role-uuid-2"] }
```
- User with `id = "0"` (admin) cannot be modified → `403`

**DELETE /api/users** — Request body:
```json
{ "ids": ["uuid-1", "uuid-2"] }
```
- User `id = "0"` filtered out silently (or explicit 403 if it's the only id)

**PUT /api/users/roles** — Request:
```json
{ "userIds": ["uuid-1", "uuid-2"], "roleIds": ["role-uuid-1"] }
```

**GET /api/users/role-chart** — Response:
```json
{ "data": [{ "name": "Admin", "count": 2 }, { "name": "Operator", "count": 5 }] }
```

---

### 3.4 Role Endpoints (`/api/roles`)

| Method | Path | Permission | Description |
|---|---|---|---|
| GET | `/api/roles` | `PERM_ROLE_LIST` | Paginated list |
| POST | `/api/roles` | `PERM_ROLE_ADD` | Create role |
| PUT | `/api/roles/{id}` | `PERM_ROLE_EDIT` | Update role |
| DELETE | `/api/roles` | `PERM_ROLE_DELETE` | Batch delete |
| GET | `/api/roles/combobox` | (any-auth) | Id+name list |

**GET /api/roles** Response rows include `authorityNames` (comma-joined or array):
```json
{
  "total": 3,
  "rows": [
    {
      "id": "...",
      "name": "Operator",
      "description": "Equipment operators",
      "authorities": [{ "id": "...", "name": "View Equipment" }]
    }
  ]
}
```

**POST /api/roles** — Request:
```json
{ "name": "Operator", "description": "...", "authorityIds": ["auth-uuid-1"] }
```

---

### 3.5 Authority Endpoints (`/api/authorities`)

| Method | Path | Permission | Description |
|---|---|---|---|
| GET | `/api/authorities` | `PERM_AUTH_LIST` | Full tree (for treegrid) |
| GET | `/api/authorities/tree` | (any-auth) | Simplified tree for combotree |
| POST | `/api/authorities` | `PERM_AUTH_ADD` | Create authority node |
| PUT | `/api/authorities/{id}` | `PERM_AUTH_EDIT` | Update authority node |
| DELETE | `/api/authorities/{id}` | `PERM_AUTH_DELETE` | Delete authority node |

**GET /api/authorities** — Returns flat list with parent reference (client builds tree):
```json
{
  "total": 12,
  "rows": [
    {
      "id": "auth-uuid",
      "parentId": null,
      "name": "System Management",
      "description": "...",
      "url": "/api/users",
      "sequence": 1
    }
  ]
}
```

**GET /api/authorities/tree** — Returns recursive tree structure:
```json
[
  {
    "id": "...",
    "name": "System Management",
    "url": "/api/users",
    "sequence": 1,
    "children": [
      { "id": "...", "name": "User List", "url": "/api/users", "sequence": 1, "children": [] }
    ]
  }
]
```

**Authority URL convention:** The `url` field in `TAUTH` is redesigned to store permission codes (e.g., `PERM_USER_LIST`) rather than raw URL paths. Spring Security checks JWT claims against these codes. See §5 for full mapping.

---

### 3.6 Menu Endpoints (`/api/menus`)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/menus/tree` | (any-auth) | Navigation menu tree |
| GET | `/api/menus` | `PERM_MENU_LIST` | Full list for admin (treegrid) |
| POST | `/api/menus` | `PERM_MENU_ADD` | Create menu node |
| PUT | `/api/menus/{id}` | `PERM_MENU_EDIT` | Update menu node |
| DELETE | `/api/menus/{id}` | `PERM_MENU_DELETE` | Delete menu node |

**GET /api/menus/tree** Response:
```json
[
  {
    "id": "...",
    "name": "System",
    "iconClass": "icon-sys",
    "url": "/users",
    "sequence": 1,
    "children": [...]
  }
]
```

---

### 3.7 Equipment Endpoints (`/api/equipment`)

| Method | Path | Permission | Description |
|---|---|---|---|
| GET | `/api/equipment` | `PERM_EQUIP_LIST` | Paginated list, optional `q` search |
| POST | `/api/equipment` | `PERM_EQUIP_ADD` | Create equipment |
| PUT | `/api/equipment/{id}` | `PERM_EQUIP_EDIT` | Update equipment |
| DELETE | `/api/equipment` | `PERM_EQUIP_DELETE` | Batch delete |
| GET | `/api/equipment/export` | `PERM_EQUIP_LIST` | Download Excel `.xlsx` |

**GET /api/equipment** — Query params: `page`, `size`, `sort` (allowed: `model`, `name`, `producer`, `quantity`), `order`, `q`

**GET /api/equipment/export** — Returns:
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Content-Disposition: `attachment; filename="equipment-export.xlsx"`
- Exports **all** records (no pagination), styled header row

---

### 3.8 Document Endpoints (`/api/documents`)

| Method | Path | Permission | Description |
|---|---|---|---|
| GET | `/api/documents` | `PERM_DOC_LIST` | Paginated list, optional `q` |
| POST | `/api/documents` | `PERM_DOC_ADD` | Create document |
| PUT | `/api/documents/{id}` | `PERM_DOC_EDIT` | Update document |
| DELETE | `/api/documents` | `PERM_DOC_DELETE` | Batch delete |
| POST | `/api/documents/{id}/manual` | `PERM_DOC_UPLOAD` | Upload manual file (multipart/form-data) |
| GET | `/api/documents/{id}/manual` | `PERM_DOC_LIST` | Download manual file |

**POST /api/documents/{id}/manual** — multipart field: `file`
Response `200`: `{ "success": true, "data": { "filename": "manual.pdf" } }`

**GET /api/documents/{id}/manual** — Streams file from configured storage path.
File names are sanitized (spaces removed) and stored in `Document.manualFilename`.

---

### 3.9 Access Log Endpoints (`/api/logs`)

| Method | Path | Permission | Description |
|---|---|---|---|
| GET | `/api/logs` | `PERM_LOG_LIST` | Paginated list, optional `q` by username |

Read-only. No write endpoints.

---

### 3.10 Online Users Endpoints (`/api/online`)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/online` | (any-auth) | Currently online users list |

Response:
```json
{
  "total": 3,
  "rows": [
    { "id": "...", "username": "alice", "ip": "192.168.1.1", "loginTime": "2024-03-13T09:00:00Z" }
  ]
}
```

**Online tracking redesign (per ADR-9):** The new system uses a `last_activity` timestamp column on the `User` entity rather than a separate `TONLINE` table. A scheduled task (every 5 min) marks users as offline if `last_activity > 30 minutes` ago. JWT-authenticated requests update `last_activity` via a filter. This is simpler than session-listener-based tracking and compatible with stateless JWT.

---

## 4. JPA Entity Design

All entities use `@Entity`, `@Table`, UUID string PKs per ADR-1. No entity is serialized directly to the API (ADR-2).

```java
// Base entity (all entities extend this)
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @Column(name = "id", length = 36)
    private String id;  // set in @PrePersist: UUID.randomUUID().toString()
}
```

### Entity Summary

| Entity Class | Table | Key Relationships |
|---|---|---|
| `User` | `t_user` | `@ManyToMany` → `Role` (via `t_user_role`) |
| `Role` | `t_role` | `@ManyToMany` → `Authority` (via `t_role_authority`) |
| `Authority` | `t_authority` | self-referential `@ManyToOne` parentId |
| `Menu` | `t_menu` | self-referential `@ManyToOne` parentId |
| `UserRole` | `t_user_role` | join entity (userId, roleId) |
| `RoleAuthority` | `t_role_authority` | join entity (roleId, authorityId) |
| `Equipment` | `t_equipment` | standalone |
| `Document` | `t_document` | standalone, `manualFilename` field |
| `AccessLog` | `t_access_log` | standalone (username String, not FK) |
| `OnlineUser` | `t_online_user` | maps online tracking; or replaced by `User.lastActivity` |

**Table naming:** snake_case with `t_` prefix, matching legacy `T` prefix semantics but lowercase for PostgreSQL compatibility.

**`User` entity key fields:**
```java
@Column(unique = true, nullable = false, length = 100)
private String username;

@Column(nullable = false, length = 100)
private String password;  // BCrypt hash (ADR-4)

@Column(name = "created_at")
private LocalDateTime createdAt;

@Column(name = "modified_at")
private LocalDateTime modifiedAt;

@Column(name = "last_activity")
private LocalDateTime lastActivity;  // for online tracking (ADR-9)

@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(name = "t_user_role",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id"))
private Set<Role> roles = new HashSet<>();
```

**`Authority` entity key field:**
```java
@Column(name = "url", length = 200)
private String url;  // stores permission CODE, e.g. "PERM_USER_LIST" (not a URL path)
```

---

## 5. Authentication & Authorization Architecture

### 5.1 Overview

```
Client (React)
    │  POST /api/auth/login {username, password}
    ▼
AuthController.login()
    │  calls UserService.authenticate()
    │  ↓ BCrypt.matches(rawPassword, storedHash)
    │  ↓ builds JWT claims: sub=userId, username, authorities=[PERM_...]
    ▼
Returns: { token: "<JWT>", username, authorities }
    │
    ├─ client stores JWT in memory (Zustand store) or sessionStorage
    │  (NOT localStorage — XSS risk)
    │
    └─ subsequent requests: Authorization: Bearer <JWT>

Spring Security Resource Server
    │  JwtAuthenticationConverter extracts claims
    │  authorities → GrantedAuthority objects: "PERM_USER_LIST", etc.
    ▼
@PreAuthorize("hasAuthority('PERM_USER_LIST')") on controllers
    OR
SecurityFilterChain requestMatchers with authority checks
```

### 5.2 JWT Structure

```json
// Header
{ "alg": "HS256", "typ": "JWT" }

// Payload
{
  "sub": "user-uuid",
  "username": "alice",
  "authorities": ["PERM_USER_LIST", "PERM_EQUIP_LIST"],
  "iat": 1710318000,
  "exp": 1710325200
}
```

**Signing algorithm:** HS256 with a 256-bit secret key (configurable, never committed to source).

For multi-server deployments, upgrade to RS256 (asymmetric). The `JwtProperties` config class accepts either a `secret` (for HS256) or `public-key-location` / `private-key-location` (for RS256) — backend engineer chooses based on deployment topology.

### 5.3 Permission Code Mapping

The `TAUTH.CURL` values in the legacy system were Struts2 action paths. In the new system they become **permission codes** stored in `t_authority.url`. The `AuthorityTreeNode` DTO exposes them as `permissionCode`.

| Legacy TAUTH.CURL | New Permission Code | Endpoint Protected |
|---|---|---|
| `userAction!datagrid.action` | `PERM_USER_LIST` | `GET /api/users` |
| `userAction!add.action` | `PERM_USER_ADD` | `POST /api/users` |
| `userAction!edit.action` | `PERM_USER_EDIT` | `PUT /api/users/{id}` |
| `userAction!delete.action` | `PERM_USER_DELETE` | `DELETE /api/users` |
| `userAction!roleEdit.action` | `PERM_USER_ROLE_EDIT` | `PUT /api/users/roles` |
| `roleAction!datagrid.action` | `PERM_ROLE_LIST` | `GET /api/roles` |
| `roleAction!add.action` | `PERM_ROLE_ADD` | `POST /api/roles` |
| `roleAction!edit.action` | `PERM_ROLE_EDIT` | `PUT /api/roles/{id}` |
| `roleAction!delete.action` | `PERM_ROLE_DELETE` | `DELETE /api/roles` |
| `authAction!treegrid.action` | `PERM_AUTH_LIST` | `GET /api/authorities` |
| `authAction!add.action` | `PERM_AUTH_ADD` | `POST /api/authorities` |
| `authAction!edit.action` | `PERM_AUTH_EDIT` | `PUT /api/authorities/{id}` |
| `authAction!delete.action` | `PERM_AUTH_DELETE` | `DELETE /api/authorities/{id}` |
| `equipAction!datagrid.action` | `PERM_EQUIP_LIST` | `GET /api/equipment` |
| `equipAction!add.action` | `PERM_EQUIP_ADD` | `POST /api/equipment` |
| `equipAction!edit.action` | `PERM_EQUIP_EDIT` | `PUT /api/equipment/{id}` |
| `equipAction!delete.action` | `PERM_EQUIP_DELETE` | `DELETE /api/equipment` |
| `equipAction!exportToExcel.action` | `PERM_EQUIP_LIST` | `GET /api/equipment/export` |
| `docAction!datagrid.action` | `PERM_DOC_LIST` | `GET /api/documents` |
| `docAction!add.action` | `PERM_DOC_ADD` | `POST /api/documents` |
| `docAction!edit.action` | `PERM_DOC_EDIT` | `PUT /api/documents/{id}` |
| `docAction!delete.action` | `PERM_DOC_DELETE` | `DELETE /api/documents` |
| `docAction!upload.action` | `PERM_DOC_UPLOAD` | `POST /api/documents/{id}/manual` |
| `logAction!datagrid.action` | `PERM_LOG_LIST` | `GET /api/logs` |

**Admin superuser:** Instead of a hardcoded `loginName.equals("admin")` bypass, the admin user is seeded with all authorities in `V2__seed.sql`. Additionally, a `ROLE_ADMIN` role is checked as a bypass in `SecurityConfig` via `.hasRole("ADMIN")` as an OR condition. This makes the bypass explicit and auditable.

### 5.4 SecurityConfig Structure

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())                          // stateless API, no CSRF
        .cors(cors -> cors.configurationSource(corsConfig()))   // allow React dev server
        .sessionManagement(sm -> sm
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()        // login, register, refresh
            .requestMatchers("/api/menus/tree").authenticated() // any logged-in user
            .requestMatchers("/api/authorities/tree").authenticated()
            .requestMatchers("/api/roles/combobox").authenticated()
            .requestMatchers("/api/online").authenticated()
            .anyRequest().authenticated()                       // default: require auth
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter())))
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(/* returns 401 JSON */)
            .accessDeniedHandler(/* returns 403 JSON */));
    return http.build();
}
```

**Method-level security** with `@EnableMethodSecurity(prePostEnabled = true)` on each controller method:
```java
@GetMapping
@PreAuthorize("hasAuthority('PERM_USER_LIST') or hasRole('ADMIN')")
public PageResponse<UserDto> listUsers(...) { ... }
```

---

## 6. AOP Logging Architecture

`AccessLogAspect` replaces `LogServiceImpl` as the logging concern.

```java
@Aspect
@Component
public class AccessLogAspect {

    // Log login and register events
    @Pointcut("execution(* com.rml.system.service.impl.UserServiceImpl.authenticate(..))" +
              " || execution(* com.rml.system.service.impl.UserServiceImpl.register(..))")
    public void logPointcut() {}

    @Around("logPointcut()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        // capture username from arg, call pjp.proceed(), record to AccessLog
        // use HttpServletRequest from RequestContextHolder for IP
    }
}
```

**Key differences from legacy:**
- Does NOT intercept the entire `UserServiceImpl.*` — only `authenticate` and `register`
- Uses `RequestContextHolder` instead of `ServletActionContext` (Struts2-specific)
- `AccessLog` is written via `LogService` (not directly in the aspect) to keep concerns separate
- Aspect is in its own `aop` package; `LogServiceImpl` is not a hybrid service+aspect

---

## 7. Flyway Migration Strategy

### 7.1 Migration File Structure

```
src/main/resources/db/migration/
  V1__schema.sql          ← DDL: create all 10 tables
  V2__seed_admin.sql      ← Admin user (BCrypt of "admin"), default roles, menus, authorities
  V3__seed_demo_data.sql  ← Demo equipment + documents (skip in prod via profile)
```

### 7.2 Schema Migration Notes

| Oracle Column | PostgreSQL/MySQL Equivalent | Notes |
|---|---|---|
| `VARCHAR2(36)` | `VARCHAR(36)` | UUID strings, unchanged |
| `VARCHAR2(100)` | `VARCHAR(100)` | straightforward |
| `NUMBER(8)` | `INTEGER` | equipment quantity |
| `NUMBER(22)` | `BIGINT` | sequence/count fields |
| `DATE` (Oracle stores time) | `TIMESTAMP` | `TDOC.CNO` discrepancy resolved: use `INTEGER` |
| `TUSER.CCREATEDATETIME` | `created_at TIMESTAMP DEFAULT NOW()` | auto-set |

### 7.3 Column Renaming Convention

Legacy Oracle columns use the `C` prefix (e.g., `CID`, `CNAME`). New schema uses lowercase snake_case without the prefix:

| Legacy | New |
|---|---|
| `CID` | `id` |
| `CNAME` | `name` (or `username` for TUSER) |
| `CPWD` | `password` |
| `CPID` | `parent_id` |
| `CCREATEDATETIME` | `created_at` |
| `CMODIFYDATETIME` | `modified_at` |
| `CURL` | `url` (stores permission code in new system) |
| `CSEQ` | `sequence` |
| `CICONCLS` | `icon_class` |
| `CMANUAL` | `manual_filename` |

### 7.4 Password Migration

`V2__seed_admin.sql` seeds the admin user with a BCrypt hash of `"admin"`:
```sql
INSERT INTO t_user (id, username, password, created_at)
VALUES ('0', 'admin', '$2a$12$<bcrypt-hash-of-admin>', NOW());
```

For migrating existing user passwords from Oracle → new DB:
- Existing MD5 hashes **cannot** be re-hashed without the original plain-text password
- **Strategy A (recommended):** Force a password reset for all non-admin users on first login. Store a flag `password_reset_required BOOLEAN` on `t_user`. After BCrypt login fails, detect the MD5 hash pattern and prompt reset.
- **Strategy B (migration utility):** Provide a standalone script that accepts a CSV of `username,plaintext_password` and updates the hash. Only viable for closed systems where passwords are known.
- [decision] ADR-11: Password migration uses Strategy A — force reset on first login for all migrated accounts. The `User` entity includes `password_reset_required BOOLEAN DEFAULT FALSE`. For migrated records from Oracle, this is set to `TRUE`.

### 7.5 Flyway Configuration

```yaml
# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false  # fresh schema only
    validate-on-migrate: true
```

For test profile: H2 in-memory with `spring.flyway.locations=classpath:db/migration,classpath:db/test` (allows test fixtures without polluting V migrations).

---

## 8. File Upload Architecture (ADR-7)

### 8.1 Storage Strategy

Files are stored on the local filesystem at a **configurable path outside the application JAR**:

```yaml
# application.yml
app:
  storage:
    base-path: /var/app/uploads    # configurable; externalized
    max-file-size: 100MB
    allowed-extensions: txt,rar,zip,doc,docx,xls,xlsx,jpg,jpeg,gif,png,pdf,wmv,avi,mp3
```

### 8.2 Upload Flow

```
POST /api/documents/{id}/manual   (multipart/form-data, field: "file")
    │
    ▼
DocumentController.uploadManual()
    │  validates file extension + size
    │  sanitizes filename (strip spaces, strip path separators)
    ▼
FileStorageService.store(file, sanitizedFilename)
    │  writes to {base-path}/{sanitizedFilename}
    │  overwrites if exists (legacy behavior)
    ▼
DocumentService.updateManualFilename(docId, sanitizedFilename)
    │  @Transactional
    │  if DB update fails → FileStorageService.delete(sanitizedFilename)
    ▼
Returns { "data": { "filename": "manual.pdf" } }
```

### 8.3 Download Flow

```
GET /api/documents/{id}/manual
    │
    ▼
DocumentController.downloadManual()
    │  loads Document.manualFilename
    ▼
FileStorageService.load(filename) → Resource
    │  returns InputStreamResource
    ▼
ResponseEntity<Resource> with:
  Content-Type: application/octet-stream (or detected MIME)
  Content-Disposition: attachment; filename="<sanitized-name>"
```

### 8.4 Future Cloud Storage Migration

`FileStorageService` is an interface. The local filesystem implementation (`LocalFileStorageService`) is swapped for an S3 implementation (`S3FileStorageService`) without changing controllers or services. This is the only change needed for containerized deployment.

---

## 9. Migration Approach

### 9.1 Phase Map

```
Phase 0: New directory scaffold (t4, t6)
    ├── backend/ Maven project with dependencies, base config
    └── frontend/ Vite + React project with routing skeleton

Phase 1: Database + Auth foundation (t5, t7)
    ├── Flyway V1 (schema), V2 (seed)
    ├── JPA entities + repositories
    ├── JWT auth endpoints
    └── Spring Security filter chain

Phase 2: Feature APIs (t8, t9)
    ├── User/Role/Authority/Menu CRUD APIs
    ├── Equipment/Document APIs + Excel export + file upload
    └── AOP logging + log API + chart data API

Phase 3: Frontend (t10, t11, t12)
    ├── Login page, layout shell, navigation menu
    ├── User/Role/Authority management UI
    ├── Equipment/Document management UI
    └── Log viewer + chart UI

Phase 4: Testing + sign-off (t13, t14)
    ├── Integration tests
    └── Feature parity verification
```

### 9.2 Breaking Changes vs. Legacy

| Legacy Behavior | New Behavior | Migration Note |
|---|---|---|
| Session-based auth (`HttpSession`) | JWT in `Authorization` header | No session cookie; client stores token |
| MD5 passwords | BCrypt | Force reset (ADR-11) |
| `TAUTH.CURL` = Struts2 URL | `t_authority.url` = permission code | `V2__seed.sql` inserts new codes; Flyway migration script transforms old values |
| Oracle `SCOTT` schema | PostgreSQL `t_` schema | New DB; no in-place migration |
| `repairAction` startup wipe | Flyway `V1`, `V2` | Startup wipe is gone; seed is idempotent via Flyway checksums |
| Server-side chart JPEG | JSON data array, client-side chart | Chart page endpoint changes completely |
| `/upload/{filename}` served as static | `GET /api/documents/{id}/manual` | URL structure changes; old bookmarks break |
| Column names `CNAME`, `CID` etc. | `name`, `id` snake_case | No column migration needed (new DB) |
| Online tracking via `HttpSessionListener` | `User.last_activity` + scheduler | `TONLINE` table dropped |
| `TMENU` seeded only via RepairService | Flyway `V2__seed.sql` + CRUD API | Menus can now be edited at runtime |
| `TDOC.CNO` type mismatch (Long vs Integer) | `INTEGER` in schema | Resolved to `INTEGER` |

### 9.3 API Path Translation Table (for frontend)

The frontend is a complete rewrite; no URL compatibility with legacy Struts2 paths is required. The React app calls `/api/v1/...` exclusively.

### 9.4 Data Migration Path (if Oracle data must be preserved)

If production data must be migrated from Oracle to PostgreSQL:

1. Export Oracle tables via `expdp` or SQLDeveloper CSV export
2. Transform CSV: rename columns, convert Oracle `DATE` to ISO timestamps, convert `CPWD` values (note: these are MD5 and will need Strategy A reset flag set)
3. Transform `TAUTH.CURL` values from action paths to permission codes using the mapping table in §5.3
4. Import via `COPY` (PostgreSQL) or `LOAD DATA` (MySQL)
5. Run Flyway `baseline` against existing schema (not applicable here since fresh schema with `V1__schema.sql`)

For most deployments, the demo/test data in the Oracle DB is not production data (it was seeded by `RepairService`). A fresh Flyway migration into the new DB is the preferred path.

---

## 10. Cross-Cutting Constraints (Binding on All Roles)

These are confirmed ADRs that all implementation tasks must follow:

| ADR | Constraint | Impact |
|---|---|---|
| ADR-1 | All PKs: `VARCHAR(36)` UUID strings, set in `@PrePersist` | Entity, DTO, DB schema |
| ADR-2 | No JPA entity serialized to API; always use DTOs | Controller, Service |
| ADR-3 | RBAC via Spring Security `@PreAuthorize` + JWT authority claims | Security config, all controllers |
| ADR-4 | BCrypt only; MD5 never used | UserService, auth flow |
| ADR-5 | Flyway migrations only; no startup data wipe | `RepairService` is NOT ported |
| ADR-6 | Sort field whitelist per endpoint; HTTP 400 on unknown | Every paginated controller |
| ADR-7 | File uploads to configurable path outside JAR; `FileStorageService` interface | DocController |
| ADR-8 | Charts as JSON data; no server-generated images | UserController `/role-chart` |
| ADR-9 | No `TONLINE`-style session tracking; use `User.last_activity` + scheduler | User entity, scheduler |
| ADR-10 | Spring Data JPA `Repository<T, ID>` per entity; no `BaseDaoI<T>` | All repository classes |
| ADR-11 | Password migration: `password_reset_required` flag; force reset on first login | User entity, auth flow |

---

## 11. Frontend Architecture Details

### 11.1 JWT Token Handling

```typescript
// store/authStore.ts (Zustand)
interface AuthState {
  token: string | null;
  username: string | null;
  authorities: string[];  // ["PERM_USER_LIST", ...]
  setAuth: (token: string, username: string, authorities: string[]) => void;
  clearAuth: () => void;
  hasAuthority: (code: string) => boolean;
}
```

Token is stored in **memory** (Zustand state). On page reload, it is lost and the user must re-login. This is the most XSS-resistant approach. `sessionStorage` is acceptable if persistence across tab refreshes is required, but `localStorage` is prohibited.

### 11.2 Axios Interceptor

```typescript
// api/client.ts
const client = axios.create({ baseURL: '/api/v1' });

client.interceptors.request.use(config => {
  const token = useAuthStore.getState().token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

client.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      useAuthStore.getState().clearAuth();
      // redirect to login
    }
    return Promise.reject(err);
  }
);
```

### 11.3 Protected Routes

```typescript
// router/ProtectedRoute.tsx
export function ProtectedRoute({ requiredAuthority }: { requiredAuthority?: string }) {
  const { token, hasAuthority } = useAuthStore();
  if (!token) return <Navigate to="/login" />;
  if (requiredAuthority && !hasAuthority(requiredAuthority)) return <Navigate to="/403" />;
  return <Outlet />;
}
```

### 11.4 CORS Configuration

Spring Boot `WebConfig`:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:5173"));  // Vite dev server
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(false);  // no cookies, JWT only
    // Prod: replace with actual frontend domain
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

In production, Vite builds a static bundle served by Spring Boot's static resource handler or a CDN, so CORS is not needed. The CORS config is dev-only and controlled by `application-dev.yml`.

### 11.5 Menu-Driven Tab Navigation (replaces EasyUI BorderLayout)

The main layout shell (`Layout.tsx`) uses:
- Ant Design `Layout` with `Sider` (menu) + `Header` + `Content`
- Ant Design `Menu` component in the sidebar, populated from `GET /api/menus/tree`
- Ant Design `Tabs` in the content area, driven by which menu items the user clicks
- Tab state is local React state in `Layout.tsx`; closing a tab removes it from state

This replaces EasyUI's `borderlayout`, `accordion`, and `tabs` pattern. The functional behavior (click to open tab, dedup open tabs, right-click context menu for close/refresh) is preserved.

---

## 12. Configuration Summary

### 12.1 Backend `application.yml` (dev)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/easyui_system
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: validate          # Flyway owns DDL; JPA only validates
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_schema: public
  flyway:
    enabled: true
    locations: classpath:db/migration

app:
  jwt:
    secret: ${JWT_SECRET}         # must be externalized; never committed
    expiration-ms: 7200000        # 2 hours
    refresh-expiration-ms: 604800000  # 7 days
  storage:
    base-path: ${UPLOAD_PATH:/var/app/uploads}
    allowed-extensions: txt,rar,zip,doc,docx,xls,xlsx,jpg,jpeg,gif,png,pdf

logging:
  level:
    com.rml.system: DEBUG
    org.springframework.security: INFO
```

### 12.2 Frontend Vite Config

```typescript
// vite.config.ts
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080'  // proxy to Spring Boot in dev
    }
  },
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') }
  }
})
```

---

## 13. Unresolved Ambiguities (Require Decision Before Implementation)

1. **Database choice:** PostgreSQL 16 vs MySQL 8? Both are supported. PostgreSQL is preferred (better UUID, JSON, full-text search support). If the deployment environment constrains to MySQL, the schema SQL needs `AUTO_INCREMENT` alternative removed (UUIDs are used anyway) and timestamp types adjusted. **[question:pm] Is there an existing database infrastructure constraint (PostgreSQL vs MySQL) for the deployment environment?**

2. **Online user tracking granularity:** ADR-9 proposes `User.last_activity` with a 5-minute scheduler. The original `TONLINE` showed the exact login time (not last activity). If exact login-time tracking is required (not just "active recently"), a `t_online_user` table should be retained. [decision] ADR-9 is the current plan; escalate to pm if exact login time is a strict requirement.

3. **Token storage (memory vs sessionStorage):** Memory-only token means the user must re-login on every page refresh. For a lab management system this may be acceptable. `sessionStorage` is a reasonable alternative with marginally more XSS exposure. This decision belongs to the frontend engineer but the security boundary is defined here.

4. **Menu CRUD:** The original system had no menu CRUD in the UI (menus were seeded by RepairService). The new design includes menu CRUD endpoints (t3 added them). If menu editing is out of scope for this rewrite phase, those endpoints can be deferred. **[notify:pm] Confirm: should menu CRUD be in scope for this rewrite or deferred?**

5. **`V3__seed_demo_data.sql` inclusion:** The original system seeded ~20 equipment items and ~15 documents as demo data. This should NOT be in `V2` (which runs in production). It should be in a separate migration activated only for dev/test profile. Backend should use `spring.flyway.locations` profile-specific override.

---

## 14. Summary ADR Register

| ADR | Decision | Binding On |
|---|---|---|
| ADR-1 | UUID string PKs (`VARCHAR(36)`) | backend, database |
| ADR-2 | Entity ↔ DTO separation; no entity serialization | backend |
| ADR-3 | REST path permission codes in `t_authority.url`; Spring Security RBAC | backend, database |
| ADR-4 | BCrypt only; MD5 removed | backend |
| ADR-5 | Flyway migrations replace RepairService | backend, database |
| ADR-6 | Sort field whitelist per endpoint; 400 on unknown | backend |
| ADR-7 | File uploads to configurable external path; `FileStorageService` interface | backend |
| ADR-8 | Chart data as JSON array; no server-side image generation | backend, frontend |
| ADR-9 | `User.last_activity` + scheduler replaces TONLINE session tracking | backend, database |
| ADR-10 | Spring Data JPA repositories per entity | backend |
| ADR-11 | Password migration: `password_reset_required` flag; force reset on first login | backend, database |

---

[decision] ADR-11: Migrated user accounts from Oracle MD5 hashes must have `password_reset_required = TRUE`. On first login, the API detects this flag and returns `{ "success": false, "code": "PASSWORD_RESET_REQUIRED", "msg": "You must reset your password" }`. Client redirects to password reset flow. This is the only supported migration path for MD5 → BCrypt conversion.

[decision] ADR-12: The `t_authority.url` column stores permission codes (e.g., `PERM_USER_LIST`), NOT REST URL paths. Spring Security endpoint-level authorization is configured in `SecurityConfig` and per-method via `@PreAuthorize`. The authority table is the source of truth for what permission codes exist; it does NOT drive dynamic URL matching at runtime.

[decision] ADR-13: Online user tracking uses `User.last_activity` timestamp field + a `@Scheduled` cleanup task. No `t_online_user` / `TONLINE` table equivalent. The `/api/online` endpoint returns users whose `last_activity` is within the past 30 minutes.

[decision] ADR-14: The new system is deployed as a Spring Boot fat JAR (`java -jar`), not a WAR on Tomcat. Static React build output is served by Spring Boot's static resource handler (placed in `src/main/resources/static/` or configured as an external path). This eliminates the `{webapp.root}/upload/` path anti-pattern entirely.

[notify:backend] The complete API contract, project layout, entity design, security config structure, and all binding ADRs (1–14) are in t3-architect.md. Pay particular attention to: ADR-12 (authority URL stores permission codes, not paths), ADR-11 (password reset flag), ADR-13 (online tracking via last_activity), and ADR-14 (JAR deployment). The `FileStorageService` must be an interface from day one.

[notify:database] Column renaming convention (§7.3) and the full schema migration notes are in t3-architect.md §7. `TDOC.CNO` type is resolved to `INTEGER`. `TUSER.CPWD` becomes `password VARCHAR(100)` (BCrypt). The `last_activity TIMESTAMP` column is added to `t_user`. Add `password_reset_required BOOLEAN DEFAULT FALSE` to `t_user`. Authority `url` column stores permission codes (e.g., `PERM_USER_LIST`), not URL paths.

[notify:frontend] API contracts in t3-architect.md §3 are the authoritative reference. Key notes: paginated endpoints return `{total, rows}`, single operations return `{success, msg, data}`. Auth flow: POST /api/auth/login returns JWT in `data.token`. Store token in Zustand (NOT localStorage). Axios interceptor pattern is specified in §11.2. Menu tree endpoint is `GET /api/menus/tree`. Chart endpoint is `GET /api/users/role-chart` returning `[{name, count}]`.

[memory] Target architecture: Spring Boot 3.5 JAR + React 19/Vite/Ant Design/Zustand. JWT auth with permission codes stored in t_authority.url (not URL paths — this is ADR-12, the most critical design change from t1 analysis). BCrypt + password_reset_required flag for migration. Flyway V1/V2/V3 strategy. FileStorageService interface for pluggable upload storage. Online tracking via User.last_activity. 14 ADRs total.
