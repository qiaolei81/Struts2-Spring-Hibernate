# Architecture Decisions

_This file will contain architecture decisions, API contracts, and technical choices made by the team._

## Project: struts2-easyui-system


- **ADR-1 [architect]** (09:27:08): ADR-1: All PKs remain UUID (VARCHAR 36) for portability.

- **ADR-2 [architect]** (09:27:09): ADR-2: Dual-model pattern (Entity ↔ DTO) preserved; no direct entity serialization to API.

- **ADR-3 [architect]** (09:27:10): ADR-3: TAUTH.CURL values redesigned to map to REST path patterns, enforced by Spring Security.

- **ADR-4 [architect]** (09:27:10): ADR-4: BCrypt replaces MD5 in new system; forced password reset or migration utility required.

- **ADR-5 [architect]** (09:27:11): ADR-5: RepairService startup wipe replaced with Flyway migrations.

- **ADR-6 [architect]** (09:27:11): ADR-6: Sort fields must be whitelisted per endpoint; HTTP 400 on unknown values.

- **ADR-7 [architect]** (09:27:12): ADR-7: File uploads stored outside webapp root; configurable path or object storage.

- **ADR-8 [architect]** (09:27:12): ADR-8: Charts returned as JSON data; client-side rendering only.

- **ADR-9 [architect]** (09:27:13): ADR-9: TONLINE session-based tracking dropped or redesigned for stateless JWT.

- **ADR-10 [architect]** (09:27:15): ADR-10: BaseDaoI<T> pattern dropped; Spring Data JPA repositories per entity.

- **ADR-11 [backend]** (09:44:50): Spring Boot 3.4.3 with Java 21 target, JJWT 0.12.7, Lombok 1.18.44 (Java 25 compatible), MapStruct 1.6.3, MySQL as primary DB target, Flyway for schema management.

- **ADR-12 [database]** (10:16:54): ADR-16: `t_access_log` uses a plain `username VARCHAR(100)` string column (no FK to `t_user`). This preserves log history when users are deleted. The `accessed_at DATETIME NOT NULL` column records the event time; `created_at`/`updated_at` from BaseEntity are also present.

- **ADR-17 [database]** (11:05:30): ADR-13: `t_role.name` values in seed data **must be UPPERCASE** (e.g. `ADMIN`, `GUEST`, `USER`). `UserDetailsServiceImpl` builds `ROLE_` + `name.toUpperCase()` — so the canonical name in DB and the `hasRole()` argument must agree after uppercasing. Never use mixed-case display names as role identifiers.

- **ADR-18 [devops]** (11:33:20): Production stack uses a single Nginx container as the only host-exposed service (port 80). Backend and DB are internal-only on `backend-net`. Nginx uses `resolver 127.0.0.11` (Docker DNS) for runtime hostname resolution, enabling backend restarts without nginx reload.

- **ADR-19 [architect]** (12:45:02): All architectural invariants and ADR-1 through ADR-18 are documented and cross-referenced in t54-architect.md. This document is the authoritative technical reference for new contributors.

- **ADR-20 [devops]** (12:49:24): ADR-21: Content-Security-Policy is set to `default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; font-src 'self' data:; connect-src 'self'; frame-ancestors 'self'; object-src 'none'; base-uri 'self'; form-action 'self'`. The `'unsafe-inline'` on `style-src` is required by Ant Design v5 CSS-in-JS and cannot be removed until Ant Design provides nonce-based CSP support.
