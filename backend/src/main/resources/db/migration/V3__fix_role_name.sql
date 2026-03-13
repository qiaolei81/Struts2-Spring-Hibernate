-- =============================================================================
-- V3__fix_role_name.sql — Fix production RBAC role name mismatch
-- Depends on:  V2__seed.sql
--
-- Problem:
--   V2 seeds the admin role with name='Administrator'. The UserDetailsServiceImpl
--   builds granted authorities as "ROLE_" + name.toUpperCase(), which produces
--   "ROLE_ADMINISTRATOR". All controllers use @PreAuthorize("hasRole('ADMIN')")
--   which Spring Security resolves to "ROLE_ADMIN". This mismatch silently
--   denies all RBAC checks for the seeded admin user in production.
--
-- Fix:
--   Rename 'Administrator' → 'ADMIN' so the authority resolves to "ROLE_ADMIN".
--
-- Migration risk: LOW — affects only the display name of role id='0'.
--   No FK references point to t_role.name; t_user_role and t_role_authority
--   reference t_role.id which remains '0' and is unchanged.
--   The 'Guest' and 'User' roles are unaffected.
-- =============================================================================

UPDATE t_role
SET    name       = 'ADMIN',
       updated_at = NOW()
WHERE  id         = '0'
  AND  name       = 'Administrator';
