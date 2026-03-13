# Architecture Analysis: struts2-easyui-system (zhibing_ssh)

**Task:** t1 — Analyze existing Struts2/Spring/Hibernate architecture, module boundaries, and integration points

---

## [scope] Read-only analysis of a single-module WAR application (files: ~75 Java, 19 JSP, 9 HBM; subsystems: 1)

---

## 1. Technology Stack

| Layer | Technology | Version |
|---|---|---|
| MVC Framework | Apache Struts 2 | 2.3.16.1 |
| IoC / AOP | Spring Framework | 3.2.8.RELEASE |
| ORM | Hibernate | 4.2.11.Final |
| Database | Oracle | 10g (SCOTT schema) |
| Connection Pool | C3P0 | 0.9.0.2 |
| Frontend | jQuery EasyUI | 1.3.1 |
| JavaScript | jQuery | 1.8.0 |
| JSON Serialization | FastJSON | 1.1.39 |
| Excel Export | Apache POI (HSSF) | 3.10-FINAL |
| Charting | JFreeChart | 1.0.17 |
| Build | Maven | WAR packaging |
| JDK | Java | 1.8 (source/target) |
| Logging | Log4j | 1.2.17 |

**Maven artifact:** `com.rml:zhibing_ssh:0.0.1-SNAPSHOT` (WAR)  
**Application title in web.xml:** `Struts2.3.16+Spring3.2.8+Hibernate4.2.11`

---

## 2. Overall Architecture

The system is a classic **monolithic server-side-rendered web application** packaged as a single WAR. There is no microservices boundary, no REST API layer, and no SPA frontend. The architecture follows a strict three-tier layering:

```
Browser (jQuery EasyUI + JSP)
        │  HTTP *.action
        ▼
[Struts2 Filter] → [Interceptor Stack] → [Action (Controller)]
                                               │
                                               ▼
                                        [Service Layer]  ←→  [AOP: Logging, Tx]
                                               │
                                               ▼
                                        [DAO Layer (Generic)]
                                               │
                                               ▼
                                     [Hibernate SessionFactory]
                                               │
                                               ▼
                                        [Oracle Database]
```

### Request Lifecycle

1. All requests matching `*.action` enter the `StrutsPrepareAndExecuteFilter`
2. `OpenSessionInViewFilter` opens a Hibernate Session before Struts processes the request (OSIV pattern)
3. Struts2 `authStack` interceptor fires: `defaultStack` → `encodingInterceptor` → `sessionInterceptor` → `authInterceptor`
4. Action method executes, calls service, which calls DAO
5. For JSON responses: Action writes directly to `HttpServletResponse` via `BaseAction.writeJson()`
6. For page navigation: Action returns a result name which maps to a JSP file

---

## 3. Spring Configuration

Two Spring XML configuration files are loaded by `ContextLoaderListener`:

### `spring-core.xml`
- Loads `config.properties` (database credentials, upload settings)
- Component-scan over `rml.dao` and `rml.service` packages
- Enables `@AspectJ` proxying (`<aop:aspectj-autoproxy />`)

### `spring-hibernate.xml`
- Defines `ComboPooledDataSource` (C3P0) — **not** Druid despite `druid.txt` present
- Defines `LocalSessionFactoryBean` (`sessionFactory`) pointing at `rml.model.po` for annotation-based entity scanning
- Defines `HibernateTransactionManager`
- Defines AOP transaction advice (see §8)

### `config.properties` (key values)
```properties
hibernate.dialect=org.hibernate.dialect.OracleDialect
driverClassName=oracle.jdbc.driver.OracleDriver
jdbc_url=jdbc:oracle:thin:@127.0.0.1:1521:orcl
jdbc_username=scott
jdbc_password=oracle
hibernate.hbm2ddl.auto=update
sessionInfoName=sessionInfo
uploadDirectory=upload
uploadFileExts=txt,rar,zip,doc,docx,xls,xlsx,jpg,jpeg,gif,png,swf,wmv,avi,wma,mp3,mid
uploadFileMaxSize=20971520    # 20 MB
```

---

## 4. Module Boundaries

There are **9 functional modules**, each with a dedicated Action, Service interface, Service implementation, and entity/VO pair:

| Module | Action | Service Interface | Service Impl | Entity (PO) | VO |
|---|---|---|---|---|---|
| User | `UserAction` | `UserServiceI` | `UserServiceImpl` | `Tuser` | `User` |
| Role | `RoleAction` | `RoleServiceI` | `RoleServiceImpl` | `Trole` | `Role` |
| Authority | `AuthAction` | `AuthServiceI` | `AuthServiceImpl` | `Tauth` | `Auth` |
| Menu | `MenuAction` | `MenuServiceI` | `MenuServiceImpl` | `Tmenu` | `Menu` |
| Equipment | `EquipAction` | `EquipServiceI` | `EquipServiceImpl` | `Tequip` | `Equip` |
| Document | `DocAction` | `DocServiceI` | `DocServiceImpl` | `Tdoc` | `Doc` |
| Log/Audit | `LogAction` | `LogServiceI` | `LogServiceImpl` | `Tlog` | `Log` |
| Online | `OnlineAction` | `OnlineServiceI` | `OnlineServiceImpl` | `Tonline` | `Online` |
| Repair/Seed | `RepairAction` | `RepairServiceI` | `RepairServiceImpl` | (all entities) | — |

**No module has its own DAO interface.** All DAO work flows through a single generic base:
```java
public interface BaseDaoI<T> { save, delete, update, find, count, executeHql, ... }
public class BaseDaoImpl<T> implements BaseDaoI<T>  // @Repository("baseDao")
```
Spring handles concurrent generic type injection across all services via type-erasure at runtime — this works because Spring injects by type hierarchy matching. All service classes declare typed fields like `private BaseDaoI<Tuser> userDao` and inject via `@Autowired` setter.

---

## 5. Database Schema

Target database: **Oracle 10g, SCOTT schema**. All table and column names are **uppercase**. All PKs are `VARCHAR2(36)` populated with `UUID.randomUUID().toString()` in Java code.

### Entity-Relationship Summary

```
TUSER ──< TUSERTROLE >── TROLE ──< TROLETAUTH >── TAUTH
                                                     │
                                                 (self-referential parent: CPID)

TMENU (self-referential: CPID)
TEQUIP   (standalone)
TDOC     (standalone)
TLOG     (standalone)
TONLINE  (standalone)
```

### Table Definitions

**TUSER**
```
CID             VARCHAR2(36)  PK
CNAME           VARCHAR2(100) NOT NULL, UNIQUE
CPWD            VARCHAR2(100) NOT NULL  (MD5 hash)
CCREATEDATETIME DATE
CMODIFYDATETIME DATE
```

**TROLE**
```
CID    VARCHAR2(36)  PK
CNAME  VARCHAR2(100) NOT NULL
CDESC  VARCHAR2(200)
```

**TAUTH** (hierarchical, self-referential)
```
CID    VARCHAR2(36)  PK
CPID   VARCHAR2(36)  FK → TAUTH.CID (nullable = root node)
CNAME  VARCHAR2(100) NOT NULL
CDESC  VARCHAR2(200)
CSEQ   NUMBER(22)    (display order)
CURL   VARCHAR2(200) (URL pattern used for authorization checks)
```

**TMENU** (hierarchical, self-referential)
```
CID      VARCHAR2(36)  PK
CPID     VARCHAR2(36)  FK → TMENU.CID
CNAME    VARCHAR2(100) NOT NULL
CICONCLS VARCHAR2(100) (EasyUI icon CSS class)
CSEQ     NUMBER(22)
CURL     VARCHAR2(200)
```

**TUSERTROLE** (join table: User ↔ Role)
```
CID      VARCHAR2(36) PK
CUSERID  VARCHAR2(36) FK → TUSER.CID
CROLEID  VARCHAR2(36) FK → TROLE.CID
```

**TROLETAUTH** (join table: Role ↔ Auth)
```
CID      VARCHAR2(36) PK
CROLEID  VARCHAR2(36) FK → TROLE.CID
CAUTHID  VARCHAR2(36) FK → TAUTH.CID
```

**TEQUIP**
```
CID       VARCHAR2(36)  PK
CMODEL    VARCHAR2(100) NOT NULL
CNAME     VARCHAR2(100)
CPRODUCER VARCHAR2(100)
CDESC     VARCHAR2(100)
CNO       NUMBER(8)     (quantity)
```

**TDOC**
```
CID       VARCHAR2(36)  PK
CMODEL    VARCHAR2(100) NOT NULL
CNAME     VARCHAR2(100)
CPRODUCER VARCHAR2(100)
CNO       NUMBER(8)     (quantity)
CMANUAL   VARCHAR2(100) (filename of uploaded manual PDF)
```

**TLOG**
```
CID       VARCHAR2(36) PK
CNAME     VARCHAR2(100) NOT NULL (username)
CIP       VARCHAR2(50)  NOT NULL
CDATETIME DATE
CMSG      VARCHAR2(200)
```

**TONLINE**
```
CID       VARCHAR2(36) PK
CNAME     VARCHAR2(100) NOT NULL
CIP       VARCHAR2(50)  NOT NULL
CDATETIME DATE          NOT NULL (last active timestamp)
```

---

## 6. Security & Authentication Architecture

### Session-Based Authentication (no Spring Security)

Login flow:
1. `UserAction.doNotNeedSession_login()` is called (bypasses session check)
2. `UserServiceImpl.login()` queries `TUSER` with MD5 hashed password
3. On success, constructs a `SessionInfo` object and stores it in `HttpSession` under key `"sessionInfo"` (configurable via `config.properties:sessionInfoName`)
4. `SessionInfo` carries: `userId`, `loginName`, `loginPassword`, `ip`, `authIds`, `authNames`, `authUrls`, `roleIds`, `roleNames`
5. `authUrls` is a comma-separated string of all `TAUTH.CURL` values the user has access to through their roles

### `SessionInfo` class (critical security context object)
```java
public class SessionInfo implements Serializable {
    String userId;      // TUSER.CID
    String loginName;   // TUSER.CNAME
    String loginPassword; // stored in session (WARNING: cleartext after login)
    String ip;          // client IP at login time
    String authIds;     // comma-delimited TAUTH.CID values
    String authNames;   // comma-delimited TAUTH.CNAME values
    String authUrls;    // comma-delimited TAUTH.CURL values (used for authorization)
    String roleIds;
    String roleNames;
}
```

### Interceptor Stack (in order)

```
authStack:
  defaultStack     (Struts2 built-in: params, validation, workflow, etc.)
  encodingInterceptor  (custom: sets response encoding)
  sessionInterceptor   (custom: checks session ≠ null; excludeMethods: doNotNeedSession_*)
  authInterceptor      (custom: checks requestPath in authUrls; excludeMethods: doNotNeedSession_*, doNotNeedAuth_*)
```

### Method Naming Convention for Security Bypass
- `doNotNeedSession_*` — skips both session AND auth checks (public endpoints)
- `doNotNeedAuth_*` — skips auth check only (session still required)

### Admin Superuser
`AuthInterceptor` hardcodes: if `sessionInfo.getLoginName().equals("admin")` → bypass all authority checks.

### Authorization Check Mechanism
```java
// AuthInterceptor
String requestPath = RequestUtil.getRequestPath(request); // e.g., "userAction!add.action"
String authUrls = sessionInfo.getAuthUrls();              // comma-separated TAUTH.CURL values
for (String url : authUrls.split(",")) {
    if (requestPath.equals(url)) { allow; }
}
```
This means `TAUTH.CURL` must match the exact Struts2 action URL path.

### Password Security
- Hashing: MD5 (unsalted), via `Encrypt.e(String)` → `Encrypt.md5(String)` → `MessageDigest("md5")`
- The MD5 hash is stored in `TUSER.CPWD`

---

## 7. Key Architectural Patterns

### 7.1 Dual-Model Pattern (PO ↔ VO)
```
rml.model.po.*  — Hibernate JPA entities (database entities, "T" prefix: Tuser, Trole, etc.)
rml.model.vo.*  — View/form objects (no JPA annotations, pagination/sorting/search fields)
```
Translation between layers: `org.springframework.beans.BeanUtils.copyProperties()`.

VOs carry extra fields that POs don't have: `ids` (comma-delimited batch IDs), `page`, `rows`, `sort`, `order`, `q` (search query), `roleIds`/`authIds` (denormalized relationship strings).

### 7.2 Single Generic DAO
One `BaseDaoImpl<T>` implementation serves all entity types. Services inject typed instances (`BaseDaoI<Tuser>`, `BaseDaoI<Trole>`, etc.) — Spring creates separate proxy instances per injection point.

### 7.3 ModelDriven Actions
All Actions implement `com.opensymphony.xwork2.ModelDriven<VO>`. This causes Struts2 to bind HTTP parameters directly to the VO object returned by `getModel()`. For example, `UserAction.getModel()` returns `new User()`, so `?cname=foo` binds directly to `user.cname`.

### 7.4 JSON Response (Mixed Controller)
Many action methods do not return a result name. Instead they call `super.writeJson(object)` which uses FastJSON to serialize an object to the HTTP response directly. The response `Content-Type` is `text/html;charset=utf-8` (not `application/json`).

The response envelope for data operations is:
```json
{ "success": true/false, "msg": "...", "obj": {...} }
```

The response envelope for paginated lists is:
```json
{ "total": 100, "rows": [...], "footer": null }
```

Tree/TreeGrid responses:
```json
[{ "id": "...", "text": "...", "state": "open/closed", "attributes": {...}, "children": [...] }]
```

### 7.5 OpenSessionInView
`OpenSessionInViewFilter` keeps the Hibernate session open for the full request lifecycle, enabling lazy-loading in JSP views and action classes. This is configured with `singleSession=true`.

---

## 8. Transaction Management

AOP-based XML transaction configuration in `spring-hibernate.xml`. **Not annotation-driven.**

```xml
<tx:advice id="transactionAdvice" transaction-manager="transactionManager">
    <tx:attributes>
        <tx:method name="add*"          propagation="REQUIRED" />
        <tx:method name="save*"         propagation="REQUIRED" />
        <tx:method name="update*"       propagation="REQUIRED" />
        <tx:method name="edit*"         propagation="REQUIRED" />
        <tx:method name="saveOrUpdate*" propagation="REQUIRED" />
        <tx:method name="delete*"       propagation="REQUIRED" />
        <tx:method name="repair*"       propagation="REQUIRED" />
        <tx:method name="upload*"       propagation="REQUIRED" />
        <tx:method name="roleEdit*"     propagation="REQUIRED" />
        <tx:method name="*"             propagation="REQUIRED" read-only="true" />
    </tx:attributes>
</tx:advice>
<aop:pointcut expression="execution(* rml.service..*Impl.*(..))" />
```

All service impl methods are transactional. Read methods get `read-only=true` but still `REQUIRED` propagation.

---

## 9. AOP: Audit Logging

`LogServiceImpl` is both a `@Service` and `@Aspect`. It intercepts `UserServiceImpl.*`:

```java
@Pointcut("execution(* rml.service.impl.UserServiceImpl.*(..))")
public void pointcut(){}

@Around("pointcut()")
public Object aroundLog(ProceedingJoinPoint point) { ... }
```

The `@Around` advice records to TLOG for `login` and `save` method calls (using method name string matching). Other methods are passed through transparently. The advice captures: username, IP (from `IpUtil.getIpAddr(ServletActionContext.getRequest())`), timestamp, success/failure message.

**Note:** `@Before` and `@After` advices exist but are commented out.

---

## 10. Server Startup Behavior

Two `ServletContextListener`s fire on startup (after `ContextLoaderListener`):

1. **`RepairListener`**: calls `repairService.repair()` — **deletes and re-seeds all data** including menus, authorities, roles, admin user, equipment catalog, document catalog. This runs on **every application startup**.

2. **`OnlineListener`**: registers as a session lifecycle listener. On session attribute add (login): inserts/updates `TONLINE`. On session destroy (logout/timeout): deletes from `TONLINE`.

The `RepairAction` also exposes manual endpoints:
- `repairAction!doNotNeedSession_repairAction.action` — re-seed without deleting
- `repairAction!doNotNeedSession_deleteAndRepair.action` — delete all and re-seed

---

## 11. File Management

### Upload
- Struts2 multi-part file upload via `DocAction`
- Files stored in `{webapp.root}/upload/` directory (relative to server deployment)
- Filename cleaned of spaces before storage
- `TDOC.CMANUAL` stores the filename (not full path)
- Access URL: `/upload/{filename}`
- `ErrorImgFilter` mapped to `/upload/*` to handle broken image refs

### Chart Generation
- `ChartUtils.getUserBarChart()` generates a JPEG via JFreeChart
- Saved to `{webapp.root}/chart/` directory
- Filename is `yyyyMMddHHmmss.jpg` (timestamp-based)
- **No cleanup**: chart images accumulate on disk indefinitely
- The `chart/` directory contains only a `Readme.txt` in source — created at runtime

---

## 12. Frontend Architecture

### Views (JSP under `src/main/webapp/`)
```
index.jsp           — Main layout shell (EasyUI borderlayout)
include.jsp         — Common CSS/JS includes for admin pages
admin/
  user.jsp          — User management (datagrid)
  userAdd.jsp       — Add user form (dialog)
  userEdit.jsp      — Edit user form (dialog)
  userRoleEdit.jsp  — Assign roles to users (dialog with tree)
  role.jsp          — Role management (datagrid)
  roleAdd.jsp / roleEdit.jsp
  auth.jsp          — Authority management (treegrid)
  authAdd.jsp / authEdit.jsp
  equip.jsp         — Equipment management (datagrid)
  equipAdd.jsp / equipEdit.jsp
  doc.jsp           — Document management (datagrid)
  docAdd.jsp / docEdit.jsp / docUpload.jsp
  log.jsp           — Access log viewer (datagrid)
  chart.jsp         — User statistics bar chart
layout/
  north.jsp / west.jsp / east.jsp / center.jsp  — BorderLayout regions
user/
  login.jsp / reg.jsp   — Login/Register dialogs
error/
  404.jsp / 500.jsp / noAuth.jsp / noSession.jsp / strutsException.jsp
```

### JavaScript Libraries
- **jQuery 1.8.0** (2012)
- **jQuery EasyUI 1.3.1** (2012)
  - `easyui.min.js`, `themes/default/easyui.css`, `themes/icon.css`
  - Components used: `datagrid`, `treegrid`, `tree`, `dialog`, `layout`, `panel`, `form`, `combobox`, `pagination`
- Custom `jslib/myUtil.js`

### Data Format Contract (Action → EasyUI)
Pagination datagrid response:
```json
{ "total": <Long>, "rows": [ {...} ] }
```
Tree response:
```json
[ { "id": "...", "text": "...", "state": "open|closed", "attributes": {"url": "..."}, "children": [...] } ]
```
CRUD operation response:
```json
{ "success": true|false, "msg": "...", "obj": <optional extra data> }
```

---

## 13. Critical Technical Risks for Migration

### Risk 1: Oracle-Specific Dependency
- `OracleDialect`, `ojdbc14`, `SELECT 1 FROM DUAL` (in C3P0 validation), Oracle `SCOTT` schema
- `ojdbc14` is not in Maven Central — requires local install
- Migration must swap dialect and driver; schema types differ (e.g., Oracle `DATE` stores time, `VARCHAR2` → `VARCHAR`)

### Risk 2: RepairService Wipes Data on Every Startup
`RepairListener` calls `repairService.repair()` on every startup, which may delete and re-seed all reference data. The `repair()` method conditionally skips if data already exists; `deleteAndRepair()` always wipes. **The new system must not replicate this pattern.** Use Flyway/Liquibase for migrations instead.

### Risk 3: Unsalted MD5 Password Hashing
All passwords are stored as unsalted MD5 hashes (`Encrypt.e()`). MD5 is cryptographically broken. New system must use BCrypt/Argon2. **Existing passwords cannot be automatically migrated** — users must reset passwords or a one-time migration must hash them with a known input.

### Risk 4: Session-Based Authorization with Comma-Delimited Strings
Authorization data (`authUrls`, `authIds`, `roleIds`) is stored as comma-delimited strings in the HTTP session. This means:
- No stateless API support (no JWT/OAuth)
- Authorization changes don't take effect until re-login
- The `authUrls` string is built at login time from TROLE → TROLETAUTH → TAUTH.CURL join
- New system must replace with proper Spring Security + JWT + database-checked authorities

### Risk 5: Sort Field HQL Injection
Several service `find()` methods directly interpolate the `sort` field from VO into HQL:
```java
hql += " order by " + equip.getSort() + " " + equip.getOrder();
```
No whitelist validation. This is an HQL injection vector. New system must use whitelist validation for sort fields.

### Risk 6: Hardcoded Admin Bypass
`AuthInterceptor` checks `loginName.equals("admin")` for full bypass. The admin user's CID is hardcoded as `"0"` in `RepairServiceImpl`. New system must represent admin access through role/permission hierarchy, not a special string.

### Risk 7: OpenSessionInView Anti-Pattern
`OpenSessionInViewFilter` enables lazy loading in views, masking N+1 queries. The service layer does not control all database access. In migration, all associations must be explicitly fetched within service methods.

### Risk 8: Struts2 Dynamic Method Invocation
`struts.enable.DynamicMethodInvocation=true` is a security risk in Struts2 that has historically enabled RCE exploits. The convention plugin mitigates but the flag being true is still dangerous. The new system must not use Struts2.

### Risk 9: JFreeChart File Accumulation
`/chart/*.jpg` files are never deleted. The new architecture should generate chart data as JSON and render on the client side (e.g., Chart.js, Recharts).

### Risk 10: Single Non-Transactional Upload Directory
Files uploaded to `{webapp.root}/upload/` are not transactionally linked to the DB record. If the DB save fails after file copy, orphaned files exist. New system must use an atomic approach (save to temp, commit DB, then move file or use S3-style object storage).

---

## 14. API Surface (Action Method → HTTP URL Mapping)

URL pattern: `http://host/{ctx}/{actionName}!{method}.action`  
All map to HTTP GET or POST (Struts2 doesn't discriminate).

### UserAction (`userAction`)
| Method | URL | Needs Auth? | Returns |
|---|---|---|---|
| `doNotNeedSession_login` | `userAction!doNotNeedSession_login.action` | No | JSON `{success, msg, obj: SessionInfo}` |
| `doNotNeedSession_logout` | `userAction!doNotNeedSession_logout.action` | No | JSON `{success}` |
| `doNotNeedSession_reg` | `userAction!doNotNeedSession_reg.action` | No | JSON `{success, msg}` |
| `user()` | `userAction!user.action` | Yes | JSP: `/admin/user.jsp` |
| `datagrid` | `userAction!datagrid.action` | Yes | JSON DataGrid |
| `add` | `userAction!add.action` | Yes | JSON `{success, msg}` |
| `edit` | `userAction!edit.action` | Yes | JSON `{success, msg}` |
| `delete` | `userAction!delete.action` | Yes | JSON `{success, msg}` |
| `roleEdit` | `userAction!roleEdit.action` | Yes | JSON `{success, msg}` |
| `chart` | `userAction!chart.action` | Yes | JSP: `/admin/chart.jsp` |

### RoleAction (`roleAction`)
| Method | URL | Needs Auth? | Returns |
|---|---|---|---|
| `role()` | `roleAction!role.action` | Yes | JSP |
| `datagrid` | `roleAction!datagrid.action` | Yes | JSON DataGrid |
| `add` | `roleAction!add.action` | Yes | JSON |
| `edit` | `roleAction!edit.action` | Yes | JSON |
| `delete` | `roleAction!delete.action` | Yes | JSON |
| `doNotNeedSession_combobox` | `roleAction!doNotNeedSession_combobox.action` | No | JSON list |

### AuthAction (`authAction`)
| Method | URL | Returns |
|---|---|---|
| `treegrid` | `authAction!treegrid.action` | JSON treegrid |
| `doNotNeedSession_tree` | public | JSON tree |
| `doNotNeedSession_treeRecursive` | public | JSON tree (recursive) |
| `add` | `authAction!add.action` | JSON |
| `edit` | `authAction!edit.action` | JSON |
| `delete` | `authAction!delete.action` | JSON |

### EquipAction (`equipAction`)
| Method | URL | Returns |
|---|---|---|
| `datagrid` | `equipAction!datagrid.action` | JSON DataGrid |
| `add` | `equipAction!add.action` | JSON |
| `edit` | `equipAction!edit.action` | JSON |
| `delete` | `equipAction!delete.action` | JSON |
| `exportToExcel` | `equipAction!exportToExcel.action` | Binary `.xls` stream |

### DocAction (`docAction`)
| Method | URL | Returns |
|---|---|---|
| `datagrid` | `docAction!datagrid.action` | JSON DataGrid |
| `add` | `docAction!add.action` | JSON |
| `edit` | `docAction!edit.action` | JSON |
| `delete` | `docAction!delete.action` | JSON |
| `upload` | `docAction!upload.action` | JSON (multipart POST) |

### LogAction (`logAction`)
| Method | URL | Returns |
|---|---|---|
| `log()` | `logAction!log.action` | JSP |
| `datagrid` | `logAction!datagrid.action` | JSON DataGrid |

### MenuAction (`menuAction`)
| Method | Returns |
|---|---|
| `doNotNeedSession_tree` | JSON tree |
| `doNotNeedSession_treeRecursive` | JSON tree recursive |
| `treegrid` | JSON treegrid |

### OnlineAction (`onlineAction`)
| Method | Returns |
|---|---|
| `doNotNeedSession_onlineDatagrid` | JSON DataGrid |

---

## 15. Cross-Cutting Constraints for New Architecture

[decision] ADR-1: All primary keys must remain UUID (VARCHAR/String) in the new schema. The existing pattern uses `UUID.randomUUID().toString()` (36-char). This preserves data portability if a migration from Oracle is needed.

[decision] ADR-2: The dual-model pattern (Entity ↔ DTO) must be preserved in the new stack. JPA entities live in the persistence layer; REST API DTOs are separate record classes. No JPA entity should be directly serialized to the API.

[decision] ADR-3: Authorization must be redesigned as URL-based RBAC where `TAUTH.CURL` values map to REST API path patterns (e.g., `GET /api/users`, `POST /api/equip`). The comma-delimited session string must be replaced by Spring Security's method/URL security.

[decision] ADR-4: Password storage must be BCrypt in the new system. A data migration plan is required — existing MD5 passwords cannot be automatically upgraded. A forced-reset flow or a one-time migration utility must be designed.

[decision] ADR-5: The `RepairService` startup seeding pattern must be replaced with Flyway/Liquibase migration scripts. Seed data (default admin, roles, menus, authorities) goes in a `V1__seed.sql` migration. Equipment/document test data goes in a separate optional migration or test fixture.

[decision] ADR-6: Sort field injection into queries must be eliminated. The new API must whitelist allowed sort fields per endpoint and reject unknown values with HTTP 400.

[decision] ADR-7: File uploads must be stored outside the webapp directory. Target: a configurable file-system path or cloud object storage. The `upload/` path inside the WAR root is incompatible with containerized deployment.

[decision] ADR-8: Chart data must be returned as JSON (role count by name), not as server-generated JPEG images. The frontend renders charts client-side (e.g., Recharts or Chart.js).

[decision] ADR-9: The `TONLINE` table tracks online users via session lifecycle events. In a JWT-based stateless API, there is no session. Online user tracking must be redesigned — either dropped or replaced with a last-activity timestamp on the User record, or a Redis-based presence system.

[decision] ADR-10: The HQL-based generic DAO with `BaseDaoI<T>` must not be carried forward. The new system uses Spring Data JPA repositories per entity, or JPQL/native queries in `@Repository` classes.

---

## 16. Implicit Assumptions (Surface for Team Discussion)

1. **The system domain** is an electronics equipment lab management system. The equipment and document catalogs contain TI, ST, Microchip, Agilent, and Tektronix part numbers. The "users" and "roles" suggest a lab access-control context.

2. **Oracle is the only supported database.** No H2/MySQL path exists in production config. The test config uses H2 (in pom.xml dependency with scope=test) but there are no test classes that exercise it.

3. **Deployment is Tomcat-based.** The WAR packaging and `${webapp.root}` upload path assumption requires a traditional servlet container. Cloud/container deployment is not considered.

4. **The `TMENU` table is not user-editable** in the current UI. There is no MenuAction CRUD — only tree/treegrid read. Menus are seeded by `RepairService`.

5. **`TAUTH.CURL` values must exactly match Struts2 action URLs** for the authorization system to work. The new system's REST paths must be designed to be stored in the `authority` table and matched by Spring Security.

6. **There is no email, no OAuth, no 2FA.** Authentication is purely username/password.

7. **The `Encrypt.main()` method reveals** the expected passwords: `md5("123456")` and `md5("abcdef")` are shown as test cases. The seeded admin password in `RepairServiceImpl` uses `Encrypt.e("admin")` (MD5 of "admin").

---

## 17. Artifact Summary

- **Total Java source files:** ~75
- **JSP views:** 19 + layout includes
- **Spring XML config files:** 2
- **Hibernate mapping files:** 9 (`.hbm.xml`, kept as reference)
- **Database tables:** 10 (TUSER, TROLE, TAUTH, TMENU, TUSERTROLE, TROLETAUTH, TEQUIP, TDOC, TLOG, TONLINE)
- **Functional modules:** 9
- **Action classes:** 10 (BaseAction + 9 feature actions)
- **Service interfaces:** 8
- **Service implementations:** 9 (LogServiceImpl doubles as AOP aspect)

---

[memory] This is an Oracle-backed Struts2/Spring3/Hibernate4 monolith with session-based auth, unsalted MD5 passwords, a single generic DAO, AOP logging on UserService, and a destructive startup repair/seed mechanism. Key migration risks: Oracle-only schema, MD5 password hashing, HQL injection via sort fields, OSIV anti-pattern, file uploads to webapp root, chart files never cleaned up, and hardcoded admin bypass.

[notify:pm] The equipment catalog and document catalog are seeded with ~20 equipment items and ~15 TI/ST/Microchip documents. These are test/demo fixtures, not production data. Confirm whether the PM's feature inventory (t2) should treat these as the representative domain objects.

[notify:backend] ADRs 1–10 above are the authoritative constraints for the new Spring Boot implementation. Pay particular attention to ADR-3 (RBAC URL mapping), ADR-4 (BCrypt migration), ADR-5 (Flyway seeding), ADR-6 (sort whitelist), and ADR-7 (file upload path). The `LogServiceImpl` pattern (AOP `@Around` on service layer) should be the model for the new AOP logging module (t9).

[notify:database] The schema section (§5) contains the complete table definitions derived from both JPA annotations and `.hbm.xml` files. Note that `TDOC.CNO` type differs between HBM (`java.lang.Long`) and JPA annotation (`Integer`) — use `INTEGER`/`BIGINT` consistently in the new schema. All PKs are UUID strings (VARCHAR 36).

[notify:frontend] The EasyUI DataGrid pagination contract is: `{total: Long, rows: [...]}`. The tree contract is: `{id, text, state, attributes: {url}, children, iconCls}`. These must be matched by the new Spring Boot REST API responses. The chart endpoint currently returns a JSP with an `<img>` tag; in the new system the `/api/users/role-chart` endpoint should return `[{name: String, count: Integer}]` for client-side rendering.
