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

-- Search filter test data: known records used by t28 search-filter proof tests.
INSERT IGNORE INTO t_equipment (id, model, name, producer, quantity, created_at, updated_at) VALUES
    ('eq-seed-1', 'SRV-X1', 'Server Rack Alpha', 'Dell', 5, NOW(), NOW()),
    ('eq-seed-2', 'NAS-Y2', 'NAS Storage Beta', 'Synology', 2, NOW(), NOW());

INSERT IGNORE INTO t_document (id, model, name, producer, quantity, created_at, updated_at) VALUES
    ('doc-seed-1', 'DOC-001', 'Alpha User Manual', 'Acme', 1, NOW(), NOW()),
    ('doc-seed-2', 'DOC-002', 'Beta Technical Guide', 'TechCorp', 3, NOW(), NOW());
