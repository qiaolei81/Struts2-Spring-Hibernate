
## [09:27:18] Notification from architect:t1
Schema section §5 of the artifact has complete table definitions. Note `TDOC.CNO` type mismatch between HBM (Long) and JPA annotation (Integer) — use `BIGINT` consistently. All PKs are UUID VARCHAR(36).

## [09:44:48] Notification from backend:t4
Backend scaffold done. Add real schema as `V1__create_initial_schema.sql` in `backend/src/main/resources/db/migration/`. UUID PKs (VARCHAR 36), ADR-1. JPA `ddl-auto=validate` — schema must exist before app starts.

## [10:09:44] Notification from pm:t14
t14 PM sign-off blocked. Backend feature layer (t7/t8/t9/t5) was never implemented — all 30+ business API endpoints return 404. Frontend is production-ready. Re-execution of t7, t8, t9, and t5 is required before any sign-off can be issued.

---
## [notify:database] from backend — 2 production data issues

### ACTION REQUIRED #1: V2__seed.sql role name
`V2__seed.sql` inserts role name `'Administrator'`. `UserDetailsServiceImpl` maps names via `"ROLE_" + name.toUpperCase()` → `ROLE_ADMINISTRATOR`. But all controllers check `hasRole('ADMIN')` = `ROLE_ADMIN`. **This will cause HTTP 403 for all authenticated requests in production.**

Fix: change the admin role name in V2__seed.sql from `'Administrator'` to `'ADMIN'`.

### ACTION REQUIRED #2: Join table extra columns (MySQL production)
`t_user_role` and `t_role_authority` in V1 schema have `id VARCHAR(36) NOT NULL`, `created_at NOT NULL`, `updated_at NOT NULL`. JPA `@ManyToMany` only inserts `(user_id, role_id)` — no id/created_at/updated_at. This will cause NOT NULL constraint violation in production MySQL.

Fix options (pick one):
A. Add DEFAULT values: `id DEFAULT (UUID())`, `created_at DEFAULT NOW()`, `updated_at DEFAULT NOW()` in a V3 migration
B. Or remove the extra columns from the join tables in V3
