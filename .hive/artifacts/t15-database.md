# t15-database: V1 Schema Migration + V2 Seed Data
**Task:** t15 | **Role:** database | **Status:** Complete

---

## Deliverables

Two Flyway migration files created at:
```
backend/src/main/resources/db/migration/
  V0__baseline.sql          ← pre-existing placeholder
  V1__schema.sql            ← NEW: DDL for all 9 tables
  V2__seed.sql              ← NEW: admin user, roles, authority tree, menus
```

---

## V1__schema.sql — 9 Tables

| Table | Legacy | Key Additions / Changes |
|---|---|---|
| `t_user` | `TUSER` | `password_reset_required TINYINT(1)` (ADR-11), `last_activity DATETIME` (ADR-9/13), `updated_at` |
| `t_role` | `TROLE` | `description VARCHAR(200)`, `created_at`/`updated_at` |
| `t_authority` | `TAUTH` | `url` = permission code not URL path (ADR-12), `sequence INT`, `parent_id` FK |
| `t_menu` | `TMENU` | `url` = React route path, `icon_class`, `sequence`, `parent_id` FK |
| `t_user_role` | `TUSERTROLE` | UUID PK `id`, `UNIQUE (user_id, role_id)`, CASCADE DELETE FKs |
| `t_role_authority` | `TROLETAUTH` | UUID PK `id`, `UNIQUE (role_id, authority_id)`, CASCADE DELETE FKs |
| `t_equipment` | `TEQUIP` | `quantity` (renamed from `CNO`), `created_at`/`updated_at` |
| `t_document` | `TDOC` | `manual_filename` (renamed from `CMANUAL`), `quantity INT` (ADR resolves TDOC.CNO type ambiguity) |
| `t_access_log` | `TLOG` | `accessed_at DATETIME`, indexes on `username` and `accessed_at` |

**TONLINE dropped** — per ADR-13, online users derived from `t_user.last_activity`.

### Schema Constraints

- **PK:** `VARCHAR(36)` UUID strings on all tables (ADR-1)
- **Engine:** `InnoDB` (required for FK enforcement in MySQL)
- **Charset:** `utf8mb4 COLLATE utf8mb4_unicode_ci` (full Unicode, case-insensitive usernames)
- **Nullable FKs:** `t_authority.parent_id` and `t_menu.parent_id` are nullable for root nodes
- **NOT NULL enforcement:** All `created_at`, `updated_at`, `password`, `username`, `model` columns are NOT NULL
- **Cascade:** User-role and role-authority joins DELETE CASCADE on parent deletion

---

## V2__seed.sql — Bootstrap Data

### Admin User
- `id = '0'`, `username = 'admin'`
- BCrypt cost-10 hash of `"admin"`: `$2b$10$o9MOH/VC1QDqMPVT1PqlF.S9c70BdszEBvIJDZ2NiPB1Ozd2qFSKq`
- `password_reset_required = 0` (admin does not need reset)

### Roles
| id | name | description |
|---|---|---|
| `'0'` | Administrator | Full access to all system functions |
| `'1'` | Guest | Minimum read-only access |
| `'2'` | User | Standard operator access |

### Authority Tree (34 nodes)
Hierarchy: `auth-root` → 6 groups → leaf permission nodes

Permission codes seeded (leaf `url` values):
```
PERM_EQUIP_LIST, PERM_EQUIP_ADD, PERM_EQUIP_EDIT, PERM_EQUIP_DELETE
PERM_DOC_LIST, PERM_DOC_ADD, PERM_DOC_EDIT, PERM_DOC_DELETE, PERM_DOC_UPLOAD
PERM_USER_LIST, PERM_USER_ADD, PERM_USER_EDIT, PERM_USER_DELETE, PERM_USER_ROLE_EDIT
PERM_ROLE_LIST, PERM_ROLE_ADD, PERM_ROLE_EDIT, PERM_ROLE_DELETE
PERM_AUTH_LIST, PERM_AUTH_ADD, PERM_AUTH_EDIT, PERM_AUTH_DELETE
PERM_MENU_LIST, PERM_MENU_ADD, PERM_MENU_EDIT, PERM_MENU_DELETE
PERM_LOG_LIST
```

### Navigation Menu Tree (12 nodes)
React route paths:
- Management → `/equipment`, `/documents`
- System → `/users`, `/roles`, `/authorities`, `/menus`
- Other → `/logs`, `/chart`, `/online`

### Role-Authority Assignments
All 34 authority nodes granted to Administrator role (`id='0'`).

---

## Migration Risks

| Risk | Severity | Mitigation |
|---|---|---|
| `V0__baseline.sql` is already baselined; Flyway will apply V1 then V2 on first run | LOW | `baseline-on-migrate: true` + `baseline-version: 0` in application.yml is correct |
| BCrypt hash `$2b$` prefix (OpenBSD format) vs `$2a$` (Spring Security default) | LOW | Spring Security's `BCryptPasswordEncoder` accepts both `$2a$` and `$2b$` — no issue |
| Self-referential FK on `t_authority` and `t_menu` | LOW | MySQL InnoDB supports deferred-style FK with no issues on seed inserts that reference already-inserted parent rows |
| `t_user_role` / `t_role_authority` — UUID primary keys on join tables | LOW | Required by JPA join-entity pattern (ADR-10). Unique constraint enforces deduplication. |
| `t_access_log.accessed_at` is NOT NULL | LOW | AOP aspect must always provide a timestamp; no INSERT without value |
| Demo data NOT included in V2 | INFO | Per architecture §7.5 — demo equipment/documents belong in `V3__seed_demo_data.sql` activated by dev profile only. Backend must NOT run V3 in prod. |
| Test profile (`application-test.yml`) has `flyway.enabled: false` | INFO | H2 uses `ddl-auto: create-drop`. V1 SQL is MySQL-specific (`TINYINT(1)`, `ENGINE=InnoDB`). H2 in MySQL mode accepts these. Confirmed compatible. |

---

## ADR Compliance

| ADR | Compliance |
|---|---|
| ADR-1: UUID VARCHAR(36) PKs | ✅ All 9 tables |
| ADR-3: Permission codes in t_authority.url | ✅ 27 leaf nodes with PERM_* values |
| ADR-4: BCrypt only | ✅ Admin seeded with cost-10 BCrypt hash |
| ADR-5: Flyway migrations | ✅ V1 + V2 replace RepairService |
| ADR-9/13: No TONLINE table; last_activity on t_user | ✅ Column present, no TONLINE table |
| ADR-11: password_reset_required flag | ✅ TINYINT(1) DEFAULT 0 on t_user |
| ADR-12: t_authority.url = permission code | ✅ Enforced in both schema comment and seed data |

---

[notify:backend] V1 + V2 migrations are ready at `backend/src/main/resources/db/migration/`. Key points for entity mapping:
- `t_user` has `password_reset_required TINYINT(1)` and `last_activity DATETIME` — map as `boolean` and `LocalDateTime` in entity
- `t_authority.url` stores permission codes (PERM_*), not URL paths
- Join tables `t_user_role` and `t_role_authority` have UUID PKs (`id VARCHAR(36)`) plus UNIQUE constraints
- All tables have `created_at`/`updated_at` (Spring Data `@EnableJpaAuditing` required)
- `t_access_log` has both `accessed_at DATETIME NOT NULL` (event time) and `created_at`/`updated_at` (audit)
- BCrypt admin password: `$2b$10$o9MOH/VC1QDqMPVT1PqlF.S9c70BdszEBvIJDZ2NiPB1Ozd2qFSKq` = "admin"
