# t24 Database: Fix Production Role Name Mismatch — COMPLETE

## What Was Done

Created `V3__fix_role_name.sql` Flyway migration to rename the seeded admin role
from `'Administrator'` to `'ADMIN'` in `t_role`.

## Root Cause

`UserDetailsServiceImpl.java` (line 42):
```java
authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()));
```

| Role name in DB   | Authority produced    | @PreAuthorize checks for | Result   |
|-------------------|-----------------------|--------------------------|----------|
| `Administrator`   | `ROLE_ADMINISTRATOR`  | `ROLE_ADMIN`             | ❌ DENIED |
| `ADMIN`           | `ROLE_ADMIN`          | `ROLE_ADMIN`             | ✅ GRANTED |

Tests passed because `TestDataSeeder` (test fixture) already created the role
as `'ADMIN'` — masking the bug in production seeds.

## Migration Created

**File:** `backend/src/main/resources/db/migration/V3__fix_role_name.sql`

```sql
UPDATE t_role
SET    name       = 'ADMIN',
       updated_at = NOW()
WHERE  id         = '0'
  AND  name       = 'Administrator';
```

**Git commit:** `2f071d80` — `fix(db): V3 migration — rename role 'Administrator' to 'ADMIN' for RBAC`

## Migration Risk Assessment

| Risk area             | Assessment                                                       |
|-----------------------|------------------------------------------------------------------|
| FK integrity          | ✅ SAFE — no FK points to `t_role.name`; only `t_role.id` is referenced |
| Idempotency           | ✅ SAFE — `WHERE name = 'Administrator'` guard prevents double-apply |
| Other roles affected  | ✅ NONE — `Guest` and `User` rows are untouched                  |
| Rollback              | Simple `UPDATE … SET name='Administrator'` if needed             |
| Flyway checksum       | ✅ V3 was not previously applied; no checksum conflict           |

## Schema Constraint Note

`t_role.name` has no UNIQUE constraint in `V1__schema.sql`. If one is added in
future, the UPDATE is still safe because `'ADMIN'` is not used by any other row.

## What the Tester Should Verify (t25)

1. Boot application against a database that has V1+V2 applied — Flyway should
   auto-apply V3 on startup.
2. Login as `admin` and confirm all `@PreAuthorize("hasRole('ADMIN')")` endpoints
   return 200, not 403.
3. All 55 existing tests continue to pass (they use `TestDataSeeder` which was
   already correct).
