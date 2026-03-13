
## [09:27:18] Notification from architect:t1
Schema section §5 of the artifact has complete table definitions. Note `TDOC.CNO` type mismatch between HBM (Long) and JPA annotation (Integer) — use `BIGINT` consistently. All PKs are UUID VARCHAR(36).

## [09:44:48] Notification from backend:t4
Backend scaffold done. Add real schema as `V1__create_initial_schema.sql` in `backend/src/main/resources/db/migration/`. UUID PKs (VARCHAR 36), ADR-1. JPA `ddl-auto=validate` — schema must exist before app starts.

## [10:09:44] Notification from pm:t14
t14 PM sign-off blocked. Backend feature layer (t7/t8/t9/t5) was never implemented — all 30+ business API endpoints return 404. Frontend is production-ready. Re-execution of t7, t8, t9, and t5 is required before any sign-off can be issued.
