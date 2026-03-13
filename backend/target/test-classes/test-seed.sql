-- Test seed data for integration tests.
-- Loaded via @Sql(BEFORE_TEST_CLASS) — must be IDEMPOTENT.
-- TestDataSeeder (Profile=test ApplicationRunner) already creates admin + roles.
-- INSERT IGNORE silently skips duplicates (H2 MySQL-compatibility mode).
-- Password for admin = "admin123"  (BCrypt hash via htpasswd -bnBC 10)

INSERT IGNORE INTO t_role (id, name, description, created_at, updated_at) VALUES
    ('0', 'ADMIN', 'Full access', NOW(), NOW()),
    ('1', 'GUEST',         'Read-only',   NOW(), NOW());

INSERT IGNORE INTO t_user (id, username, password, password_reset_required, last_activity, created_at, updated_at) VALUES
    ('0', 'admin', '$2y$10$oy8ExjdX93yzUQ/mNdD3u.AMR/ntVRXaETg2c5IjtfTJd5QcoCZ4q', 0, NULL, NOW(), NOW());

-- H2 @ManyToMany join table has no 'id' column; use composite (user_id, role_id).
INSERT IGNORE INTO t_user_role (user_id, role_id) VALUES ('0', '0');
