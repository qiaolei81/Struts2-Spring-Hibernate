-- =============================================================================
-- V1__schema.sql — Initial schema for the Spring Boot rewrite
-- Target:    MySQL 8.x (InnoDB, utf8mb4)
-- Naming:    snake_case with t_ prefix; no legacy C-prefix columns
-- PKs:       VARCHAR(36) UUID strings per ADR-1
-- Auditing:  created_at / updated_at on every table (Spring Data @EnableJpaAuditing)
-- Notable:
--   t_user.password stores BCrypt hash (ADR-4); VARCHAR(100) holds $2b$10$... (60 chars)
--   t_user.last_activity supports online-user tracking via scheduler (ADR-9/ADR-13)
--   t_user.password_reset_required supports MD5→BCrypt migration path (ADR-11)
--   t_authority.url stores permission code e.g. PERM_USER_LIST, NOT a URL path (ADR-3/ADR-12)
--   t_user_role and t_role_authority are join-entity tables with their own UUID PK
--   TONLINE table is dropped; online tracking uses t_user.last_activity (ADR-13)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. t_user
-- -----------------------------------------------------------------------------
CREATE TABLE t_user (
    id                      VARCHAR(36)     NOT NULL,
    username                VARCHAR(100)    NOT NULL,
    password                VARCHAR(100)    NOT NULL COMMENT 'BCrypt hash (ADR-4)',
    password_reset_required TINYINT(1)      NOT NULL DEFAULT 0 COMMENT 'Force BCrypt reset on migrated MD5 accounts (ADR-11)',
    last_activity           DATETIME                 COMMENT 'Used by /api/online endpoint (ADR-9/ADR-13)',
    created_at              DATETIME        NOT NULL,
    updated_at              DATETIME        NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. t_role
-- -----------------------------------------------------------------------------
CREATE TABLE t_role (
    id          VARCHAR(36)     NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    description VARCHAR(200),
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME        NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. t_authority  (permission nodes; url = permission code per ADR-3/ADR-12)
-- -----------------------------------------------------------------------------
CREATE TABLE t_authority (
    id          VARCHAR(36)     NOT NULL,
    parent_id   VARCHAR(36)              COMMENT 'Self-referential parent (nullable for root nodes)',
    name        VARCHAR(100)    NOT NULL,
    description VARCHAR(200),
    url         VARCHAR(200)             COMMENT 'Permission code e.g. PERM_USER_LIST (ADR-12)',
    sequence    INT                      COMMENT 'Display order among siblings',
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_auth_parent FOREIGN KEY (parent_id) REFERENCES t_authority (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. t_menu  (navigation menu; url = React frontend route path)
-- -----------------------------------------------------------------------------
CREATE TABLE t_menu (
    id          VARCHAR(36)     NOT NULL,
    parent_id   VARCHAR(36)              COMMENT 'Self-referential parent (nullable for root nodes)',
    name        VARCHAR(100)    NOT NULL,
    icon_class  VARCHAR(100)             COMMENT 'CSS icon class e.g. icon-sum',
    url         VARCHAR(200)             COMMENT 'React route path e.g. /equipment',
    sequence    INT                      COMMENT 'Display order among siblings',
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_menu_parent FOREIGN KEY (parent_id) REFERENCES t_menu (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. t_user_role  (User ↔ Role many-to-many join entity)
-- -----------------------------------------------------------------------------
CREATE TABLE t_user_role (
    id          VARCHAR(36)     NOT NULL,
    user_id     VARCHAR(36)     NOT NULL,
    role_id     VARCHAR(36)     NOT NULL,
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME        NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES t_role (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. t_role_authority  (Role ↔ Authority many-to-many join entity)
-- -----------------------------------------------------------------------------
CREATE TABLE t_role_authority (
    id           VARCHAR(36)    NOT NULL,
    role_id      VARCHAR(36)    NOT NULL,
    authority_id VARCHAR(36)    NOT NULL,
    created_at   DATETIME       NOT NULL,
    updated_at   DATETIME       NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_authority (role_id, authority_id),
    CONSTRAINT fk_ra_role      FOREIGN KEY (role_id)      REFERENCES t_role      (id) ON DELETE CASCADE,
    CONSTRAINT fk_ra_authority FOREIGN KEY (authority_id) REFERENCES t_authority (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. t_equipment
-- -----------------------------------------------------------------------------
CREATE TABLE t_equipment (
    id          VARCHAR(36)     NOT NULL,
    model       VARCHAR(100)    NOT NULL COMMENT 'Equipment model number',
    name        VARCHAR(100),
    producer    VARCHAR(100),
    description VARCHAR(200),
    quantity    INT              COMMENT 'Stock count (legacy: CNO)',
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME        NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. t_document
-- -----------------------------------------------------------------------------
CREATE TABLE t_document (
    id               VARCHAR(36)     NOT NULL,
    model            VARCHAR(100)    NOT NULL COMMENT 'Component/device model number',
    name             VARCHAR(100),
    producer         VARCHAR(100),
    quantity         INT              COMMENT 'Stock count (legacy: CNO)',
    manual_filename  VARCHAR(100)     COMMENT 'Stored filename for uploaded manual PDF (ADR-7)',
    created_at       DATETIME        NOT NULL,
    updated_at       DATETIME        NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 9. t_access_log  (AOP-written access log; read-only from API per §3.9)
-- -----------------------------------------------------------------------------
CREATE TABLE t_access_log (
    id          VARCHAR(36)     NOT NULL,
    username    VARCHAR(100)    NOT NULL COMMENT 'Login name (plain string, no FK — survives user deletion)',
    ip          VARCHAR(50),
    accessed_at DATETIME        NOT NULL COMMENT 'Time of the logged event',
    message     VARCHAR(200),
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME        NOT NULL,
    PRIMARY KEY (id),
    KEY idx_log_username (username),
    KEY idx_log_accessed_at (accessed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
