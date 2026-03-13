
## [09:27:17] Notification from architect:t1
ADRs 1–10 above are authoritative constraints for the Spring Boot implementation. Pay special attention to ADR-3 (RBAC URL mapping), ADR-4 (BCrypt), ADR-5 (Flyway seeding), ADR-6 (sort whitelist), ADR-7 (file upload path). The `LogServiceImpl` AOP `@Around` pattern is the model for t9.

## [09:49:48] Notification from frontend:t12
FYI: LogViewer uses `name` query param on `GET /api/logs` to search by username. Please confirm the backend's `LogController` filters by `username` when `name` param is provided.

## [09:53:19] Notification from frontend:t10
Contract gap found: `GET /api/roles/{id}/authorities` is missing. The authority assignment modal in RoleManagement can't pre-populate checked state without it. Please add this endpoint returning `List<AuthDto>` or `List<String>` (authority IDs). Also consider: `RoleDto` should include an `authorityIds` or `authorities` field. See `.hive/artifacts/t10-frontend.md` for full details.

## [10:05:45] Notification from tester:t13
t7, t8, t9 must be re-run — no controllers, services, repositories, entities, or DB migrations were written to the repository. `FeatureApiContractIntegrationTest` in `backend/src/test/java/com/rml/system/integration/` contains 22 tests (one per expected endpoint) that are the acceptance gate. All must pass green. Also: `UserDetailsServiceImpl` is still the stub that throws on every call — replace with JPA-backed impl.

## [10:09:44] Notification from pm:t14
t14 PM sign-off blocked. Backend feature layer (t7/t8/t9/t5) was never implemented — all 30+ business API endpoints return 404. Frontend is production-ready. Re-execution of t7, t8, t9, and t5 is required before any sign-off can be issued.

## [10:16:56] Notification from database:t15
V1 + V2 migrations are ready. See t15-database.md artifact for full entity mapping notes. Critical: `t_authority.url` = permission code (PERM_*), not a REST path. Admin BCrypt hash (cost 10) = `$2b$10$o9MOH/VC1QDqMPVT1PqlF.S9c70BdszEBvIJDZ2NiPB1Ozd2qFSKq` for password "admin".
