# Task Board

## Current Tasks

- 🔄 `t34` **Commit all untracked production deployment artifacts and uncommitted test improvements to git** (devops) - in_progress
- ⏳ `t35` **Final delivery sign-off: confirm all artifacts committed, repo is clean, and rewrite is fully delivered with no remaining gaps** (pm) - pending

## Completed Tasks

- ✅ `t1` **Analyze existing Struts2/Spring/Hibernate architecture, module boundaries, and integration points** (architect)
- ✅ `t2` **Inventory all existing features, user flows, and acceptance criteria from the current system** (pm)
- ✅ `t3` **Design target Spring Boot + React architecture: API contracts, project layout, auth strategy, and migration approach for the new directory** (architect)
- ✅ `t4` **Scaffold Spring Boot project in new directory with Maven structure, dependencies, and base configuration** (backend)
- ✅ `t5` **Design and scaffold relational schema, JPA entities, repositories, and migration scripts targeting the new stack** (database)
- ✅ `t6` **Scaffold React project in new directory with routing, state management, build config, and shared component foundation** (frontend)
- ✅ `t7` **Implement user, role, and authority management REST APIs with Spring Security RBAC and JWT authentication** (backend)
- ✅ `t8` **Implement equipment and document management REST APIs including Excel export via Apache POI** (backend)
- ✅ `t9` **Implement AOP-based access logging and statistics/chart data REST APIs** (backend)
- ✅ `t10` **Implement login, user management, role management, and authority assignment UI in React** (frontend)
- ✅ `t11` **Implement equipment and document management UI with data tables and Excel export trigger in React** (frontend)
- ✅ `t12` **Implement user statistics charts and access log viewer UI in React** (frontend)
- ✅ `t13` **Write and run integration and end-to-end tests covering all major feature flows in the new stack** (tester)
- ✅ `t14` **Verify feature parity against original inventory, validate all user flows, and sign off on the rewrite** (pm)
- ✅ `t15` **Write V1 schema migration (all 9 tables) and V2 seed data for the new stack** (database)
- ✅ `t16` **Implement all missing backend feature layer: entities, repositories, services, and controllers for auth/user/role/authority/equipment/document/log/stats/menu/online-users, plus AOP access logging and UserDetailsService** (backend)
- ✅ `t17` **Implement OnlineUsers page and replace hardcoded sidebar with dynamic menu loaded from GET /api/menus/tree** (frontend)
- ✅ `t18` **Re-run all integration and E2E tests; verify all 22 contract tests pass and full login-to-CRUD flow is proven end-to-end** (tester)
- ✅ `t19` **Final feature parity sign-off: validate all user flows match original inventory and grant rewrite acceptance** (pm)
- ✅ `t20` **Implement service layer, all controllers, real UserDetailsService, AccessLogAspect, and fix GlobalExceptionHandler 404 handling** (backend)
- ✅ `t21` **Re-run all integration and E2E tests: verify all 22 contract tests pass, remove @Disabled from PmVerificationScenariosTest and confirm all 7 PM scenarios pass, prove login-to-CRUD flow end-to-end** (tester)
- ✅ `t22` **Final feature parity sign-off: validate all acceptance criteria from t2 inventory are met, confirm all user flows work end-to-end, and grant rewrite acceptance** (pm)
- ✅ `t23` **Commit all implemented backend and frontend code to git with proper structure and documentation** (backend)
- ✅ `t24` **Fix production role name mismatch: V3 migration to rename 'Administrator' to 'ADMIN' in t_role seed data** (database)
- ✅ `t25` **Verify V3 migration, run full test suite, and confirm production RBAC works end-to-end after role name fix** (tester)
- ✅ `t26` **Final sign-off: confirm role name fix resolves production RBAC, all 55 tests pass, and rewrite is production-ready** (pm)
- ✅ `t27` **Fix search param mismatch: align backend controllers to accept 'name' query param for filtering** (backend)
- ✅ `t28` **Verify search param fix: re-run full test suite and confirm search filters work end-to-end** (tester)
- ✅ `t29` **Final sign-off: confirm search fix is complete and rewrite is fully production-ready with no known defects** (pm)
- ✅ `t30` **Validate staging MySQL deployment: apply V1-V3 Flyway migrations, smoke test RBAC and all endpoints** (devops)
- ✅ `t31` **Production deployment: write Docker Compose, Dockerfile for backend and frontend, Nginx reverse proxy config, and environment variable documentation** (devops)
- ✅ `t32` **Final production readiness review: validate Docker Compose stack builds and starts end-to-end, confirm all services healthy** (devops)
- ✅ `t33` **Final acceptance sign-off: confirm production deployment artifacts complete, rewrite is fully delivered with no remaining defects** (pm)
