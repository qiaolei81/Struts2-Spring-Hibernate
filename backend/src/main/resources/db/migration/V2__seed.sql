-- =============================================================================
-- V2__seed.sql — Bootstrap data for the Spring Boot rewrite
-- Depends on:  V1__schema.sql
-- Contents:
--   1. Admin user  (id='0', BCrypt of "admin", password_reset_required=0)
--   2. Roles       (Administrator id='0', Guest id='1', User id='2')
--   3. Authority tree (permission codes per ADR-3/ADR-12)
--   4. Navigation menu tree (React route paths)
--   5. Admin user → Administrator role mapping
--   6. Administrator role → all authorities mapping
-- =============================================================================
-- IMPORTANT: Do NOT add demo equipment/documents here.
--   That data belongs in V3__seed_demo_data.sql, activated only in dev/test.
-- =============================================================================

-- ─── 1. ADMIN USER ───────────────────────────────────────────────────────────
-- Password: "admin"  |  BCrypt cost 10
-- Hash verified against Spring Security BCryptPasswordEncoder (strength 10)
INSERT INTO t_user (id, username, password, password_reset_required, last_activity, created_at, updated_at)
VALUES (
    '0',
    'admin',
    '$2b$10$o9MOH/VC1QDqMPVT1PqlF.S9c70BdszEBvIJDZ2NiPB1Ozd2qFSKq',
    0,
    NULL,
    NOW(),
    NOW()
);

-- ─── 2. ROLES ────────────────────────────────────────────────────────────────
INSERT INTO t_role (id, name, description, created_at, updated_at) VALUES
    ('0', 'Administrator', 'Full access to all system functions',  NOW(), NOW()),
    ('1', 'Guest',         'Minimum read-only access',             NOW(), NOW()),
    ('2', 'User',          'Standard operator access',             NOW(), NOW());

-- ─── 3. AUTHORITY TREE ───────────────────────────────────────────────────────
-- url column stores permission code (ADR-12).
-- Root nodes and category nodes have url=NULL (they are grouping labels only).
-- Leaf nodes carry the permission code enforced by Spring Security @PreAuthorize.

-- Root
INSERT INTO t_authority (id, parent_id, name, description, url, sequence, created_at, updated_at) VALUES
    ('auth-root', NULL, 'System', 'Root authority node', NULL, 1, NOW(), NOW());

-- ── Management group (equipment + documents) ─────────────────────────────────
INSERT INTO t_authority (id, parent_id, name, description, url, sequence, created_at, updated_at) VALUES
    ('auth-mgmt',              'auth-root', 'Management',         'Equipment and document management', NULL,                  1, NOW(), NOW()),
    ('auth-equip-list',        'auth-mgmt', 'View Equipment',     'List and search equipment',         'PERM_EQUIP_LIST',     1, NOW(), NOW()),
    ('auth-equip-add',         'auth-mgmt', 'Add Equipment',      'Create new equipment record',       'PERM_EQUIP_ADD',      2, NOW(), NOW()),
    ('auth-equip-edit',        'auth-mgmt', 'Edit Equipment',     'Modify existing equipment',         'PERM_EQUIP_EDIT',     3, NOW(), NOW()),
    ('auth-equip-delete',      'auth-mgmt', 'Delete Equipment',   'Remove equipment records',          'PERM_EQUIP_DELETE',   4, NOW(), NOW()),
    ('auth-doc-list',          'auth-mgmt', 'View Documents',     'List and search documents',         'PERM_DOC_LIST',       5, NOW(), NOW()),
    ('auth-doc-add',           'auth-mgmt', 'Add Document',       'Create new document record',        'PERM_DOC_ADD',        6, NOW(), NOW()),
    ('auth-doc-edit',          'auth-mgmt', 'Edit Document',      'Modify existing document',          'PERM_DOC_EDIT',       7, NOW(), NOW()),
    ('auth-doc-delete',        'auth-mgmt', 'Delete Document',    'Remove document records',           'PERM_DOC_DELETE',     8, NOW(), NOW()),
    ('auth-doc-upload',        'auth-mgmt', 'Upload Manual',      'Upload device manual file',         'PERM_DOC_UPLOAD',     9, NOW(), NOW());

-- ── User management group ─────────────────────────────────────────────────────
INSERT INTO t_authority (id, parent_id, name, description, url, sequence, created_at, updated_at) VALUES
    ('auth-user',              'auth-root', 'User Management',    'User account management',           NULL,                  2, NOW(), NOW()),
    ('auth-user-list',         'auth-user', 'View Users',         'List and search user accounts',     'PERM_USER_LIST',      1, NOW(), NOW()),
    ('auth-user-add',          'auth-user', 'Add User',           'Create new user account',           'PERM_USER_ADD',       2, NOW(), NOW()),
    ('auth-user-edit',         'auth-user', 'Edit User',          'Modify existing user account',      'PERM_USER_EDIT',      3, NOW(), NOW()),
    ('auth-user-delete',       'auth-user', 'Delete User',        'Remove user accounts',              'PERM_USER_DELETE',    4, NOW(), NOW()),
    ('auth-user-role-edit',    'auth-user', 'Assign Roles',       'Batch-assign roles to users',       'PERM_USER_ROLE_EDIT', 5, NOW(), NOW());

-- ── Role management group ─────────────────────────────────────────────────────
INSERT INTO t_authority (id, parent_id, name, description, url, sequence, created_at, updated_at) VALUES
    ('auth-role',              'auth-root', 'Role Management',    'Role definition and management',    NULL,                  3, NOW(), NOW()),
    ('auth-role-list',         'auth-role', 'View Roles',         'List and search roles',             'PERM_ROLE_LIST',      1, NOW(), NOW()),
    ('auth-role-add',          'auth-role', 'Add Role',           'Create new role',                   'PERM_ROLE_ADD',       2, NOW(), NOW()),
    ('auth-role-edit',         'auth-role', 'Edit Role',          'Modify existing role',              'PERM_ROLE_EDIT',      3, NOW(), NOW()),
    ('auth-role-delete',       'auth-role', 'Delete Role',        'Remove roles',                      'PERM_ROLE_DELETE',    4, NOW(), NOW());

-- ── Authority management group ────────────────────────────────────────────────
INSERT INTO t_authority (id, parent_id, name, description, url, sequence, created_at, updated_at) VALUES
    ('auth-auth',              'auth-root', 'Authority Management', 'Permission node management',     NULL,                  4, NOW(), NOW()),
    ('auth-auth-list',         'auth-auth', 'View Authorities',   'View authority tree',               'PERM_AUTH_LIST',      1, NOW(), NOW()),
    ('auth-auth-add',          'auth-auth', 'Add Authority',      'Create new authority node',         'PERM_AUTH_ADD',       2, NOW(), NOW()),
    ('auth-auth-edit',         'auth-auth', 'Edit Authority',     'Modify existing authority node',    'PERM_AUTH_EDIT',      3, NOW(), NOW()),
    ('auth-auth-delete',       'auth-auth', 'Delete Authority',   'Remove authority nodes',            'PERM_AUTH_DELETE',    4, NOW(), NOW());

-- ── Menu management group ─────────────────────────────────────────────────────
INSERT INTO t_authority (id, parent_id, name, description, url, sequence, created_at, updated_at) VALUES
    ('auth-menu',              'auth-root', 'Menu Management',    'Navigation menu management',        NULL,                  5, NOW(), NOW()),
    ('auth-menu-list',         'auth-menu', 'View Menus',         'View menu tree',                    'PERM_MENU_LIST',      1, NOW(), NOW()),
    ('auth-menu-add',          'auth-menu', 'Add Menu',           'Create new menu node',              'PERM_MENU_ADD',       2, NOW(), NOW()),
    ('auth-menu-edit',         'auth-menu', 'Edit Menu',          'Modify existing menu node',         'PERM_MENU_EDIT',      3, NOW(), NOW()),
    ('auth-menu-delete',       'auth-menu', 'Delete Menu',        'Remove menu nodes',                 'PERM_MENU_DELETE',    4, NOW(), NOW());

-- ── Other group (logs, stats, online) ────────────────────────────────────────
INSERT INTO t_authority (id, parent_id, name, description, url, sequence, created_at, updated_at) VALUES
    ('auth-other',             'auth-root', 'Other',              'Miscellaneous functions',           NULL,                  6, NOW(), NOW()),
    ('auth-log-list',          'auth-other', 'View Access Logs',  'View user access log',              'PERM_LOG_LIST',       1, NOW(), NOW());

-- ─── 4. NAVIGATION MENU TREE ─────────────────────────────────────────────────
-- url = React frontend route path (e.g. /equipment)
-- Root node has url='' and acts as the container; not rendered directly.

INSERT INTO t_menu (id, parent_id, name, icon_class, url, sequence, created_at, updated_at) VALUES
    ('menu-root',  NULL,        'Home',       'icon-save', '',            1, NOW(), NOW());

-- ── Management section ────────────────────────────────────────────────────────
INSERT INTO t_menu (id, parent_id, name, icon_class, url, sequence, created_at, updated_at) VALUES
    ('menu-mgmt',  'menu-root', 'Management', 'icon-sum',  '',            1, NOW(), NOW()),
    ('menu-equip', 'menu-mgmt', 'Equipment',  NULL,        '/equipment',  1, NOW(), NOW()),
    ('menu-doc',   'menu-mgmt', 'Document',   NULL,        '/documents',  2, NOW(), NOW());

-- ── System section ────────────────────────────────────────────────────────────
INSERT INTO t_menu (id, parent_id, name, icon_class, url, sequence, created_at, updated_at) VALUES
    ('menu-sys',   'menu-root', 'System',     'icon-sum',  '',            2, NOW(), NOW()),
    ('menu-user',  'menu-sys',  'User',       NULL,        '/users',      1, NOW(), NOW()),
    ('menu-role',  'menu-sys',  'Role',       NULL,        '/roles',      2, NOW(), NOW()),
    ('menu-auth',  'menu-sys',  'Authority',  NULL,        '/authorities',3, NOW(), NOW()),
    ('menu-menu',  'menu-sys',  'Menu',       NULL,        '/menus',      4, NOW(), NOW());

-- ── Other section ─────────────────────────────────────────────────────────────
INSERT INTO t_menu (id, parent_id, name, icon_class, url, sequence, created_at, updated_at) VALUES
    ('menu-other',   'menu-root', 'Other',         'icon-sum', '',        3, NOW(), NOW()),
    ('menu-log',     'menu-other','Access Log',     NULL,       '/logs',   1, NOW(), NOW()),
    ('menu-chart',   'menu-other','Statistics',     NULL,       '/chart',  2, NOW(), NOW()),
    ('menu-online',  'menu-other','Online Users',   NULL,       '/online', 3, NOW(), NOW());

-- ─── 5. ADMIN USER → ADMINISTRATOR ROLE ──────────────────────────────────────
INSERT INTO t_user_role (id, user_id, role_id, created_at, updated_at) VALUES
    ('ur-admin-0', '0', '0', NOW(), NOW());

-- ─── 6. ADMINISTRATOR ROLE → ALL AUTHORITIES ─────────────────────────────────
-- Every leaf and category node is granted to the Administrator role.
-- This replicates the RepairServiceImpl.repairRoleAuth() logic that assigned
-- all Tauth records to role CID='0'.
INSERT INTO t_role_authority (id, role_id, authority_id, created_at, updated_at) VALUES
    ('ra-0-auth-root',          '0', 'auth-root',          NOW(), NOW()),
    ('ra-0-auth-mgmt',          '0', 'auth-mgmt',          NOW(), NOW()),
    ('ra-0-auth-equip-list',    '0', 'auth-equip-list',    NOW(), NOW()),
    ('ra-0-auth-equip-add',     '0', 'auth-equip-add',     NOW(), NOW()),
    ('ra-0-auth-equip-edit',    '0', 'auth-equip-edit',    NOW(), NOW()),
    ('ra-0-auth-equip-delete',  '0', 'auth-equip-delete',  NOW(), NOW()),
    ('ra-0-auth-doc-list',      '0', 'auth-doc-list',      NOW(), NOW()),
    ('ra-0-auth-doc-add',       '0', 'auth-doc-add',       NOW(), NOW()),
    ('ra-0-auth-doc-edit',      '0', 'auth-doc-edit',      NOW(), NOW()),
    ('ra-0-auth-doc-delete',    '0', 'auth-doc-delete',    NOW(), NOW()),
    ('ra-0-auth-doc-upload',    '0', 'auth-doc-upload',    NOW(), NOW()),
    ('ra-0-auth-user',          '0', 'auth-user',          NOW(), NOW()),
    ('ra-0-auth-user-list',     '0', 'auth-user-list',     NOW(), NOW()),
    ('ra-0-auth-user-add',      '0', 'auth-user-add',      NOW(), NOW()),
    ('ra-0-auth-user-edit',     '0', 'auth-user-edit',     NOW(), NOW()),
    ('ra-0-auth-user-delete',   '0', 'auth-user-delete',   NOW(), NOW()),
    ('ra-0-auth-user-role-edit','0', 'auth-user-role-edit',NOW(), NOW()),
    ('ra-0-auth-role',          '0', 'auth-role',          NOW(), NOW()),
    ('ra-0-auth-role-list',     '0', 'auth-role-list',     NOW(), NOW()),
    ('ra-0-auth-role-add',      '0', 'auth-role-add',      NOW(), NOW()),
    ('ra-0-auth-role-edit',     '0', 'auth-role-edit',     NOW(), NOW()),
    ('ra-0-auth-role-delete',   '0', 'auth-role-delete',   NOW(), NOW()),
    ('ra-0-auth-auth',          '0', 'auth-auth',          NOW(), NOW()),
    ('ra-0-auth-auth-list',     '0', 'auth-auth-list',     NOW(), NOW()),
    ('ra-0-auth-auth-add',      '0', 'auth-auth-add',      NOW(), NOW()),
    ('ra-0-auth-auth-edit',     '0', 'auth-auth-edit',     NOW(), NOW()),
    ('ra-0-auth-auth-delete',   '0', 'auth-auth-delete',   NOW(), NOW()),
    ('ra-0-auth-menu',          '0', 'auth-menu',          NOW(), NOW()),
    ('ra-0-auth-menu-list',     '0', 'auth-menu-list',     NOW(), NOW()),
    ('ra-0-auth-menu-add',      '0', 'auth-menu-add',      NOW(), NOW()),
    ('ra-0-auth-menu-edit',     '0', 'auth-menu-edit',     NOW(), NOW()),
    ('ra-0-auth-menu-delete',   '0', 'auth-menu-delete',   NOW(), NOW()),
    ('ra-0-auth-other',         '0', 'auth-other',         NOW(), NOW()),
    ('ra-0-auth-log-list',      '0', 'auth-log-list',      NOW(), NOW());
