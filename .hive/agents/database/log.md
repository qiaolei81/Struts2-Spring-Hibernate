# Database Execution Log

_This file will contain execution history for the database agent._

---

## [2026-03-13 10:16:57] t15: Write V1 schema migration (all 9 tables) and V2 seed data for the new stack
MySQL-targeted migrations at backend/src/main/resources/db/migration/. V1: 9 tables (no TONLINE per ADR-13). V2: 34 authority nodes with PERM_* codes, 12 menu nodes with React paths, admin user (id='0') with BCrypt hash. All 34 authorities granted to Administrator role. Demo data intentionally excluded — belongs in V3 (dev profile only).
