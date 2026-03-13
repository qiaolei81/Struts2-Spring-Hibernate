-- Clean up test data created during integration tests.
-- Runs after each test class via @Sql(executionPhase = AFTER_TEST_CLASS).
-- Preserves seed data: admin user (id='0'), roles (id='0','1').

DELETE FROM t_access_log;
DELETE FROM t_document;
DELETE FROM t_equipment;
DELETE FROM t_user_role WHERE user_id != '0';
DELETE FROM t_user WHERE id != '0';
DELETE FROM t_role WHERE id NOT IN ('0', '1');
DELETE FROM t_role_authority;
DELETE FROM t_authority;
DELETE FROM t_menu;
