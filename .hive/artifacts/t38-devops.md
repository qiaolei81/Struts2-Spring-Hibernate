# t38 DevOps Artifact — Open PR from Fork to Upstream

**Task:** t38 | **Role:** DevOps | **Status:** ✅ COMPLETE  
**Date:** 2026-03-13

---

## Scope Assessment

[scope] Single PR creation from fork to upstream; no code changes required (files: 0 modified; subsystems: 1)

---

## PR Created

**URL:** https://github.com/KevinXie0131/Struts2-Spring-Hibernate/pull/1  
**Number:** #1  
**State:** OPEN  
**Author:** qiaolei81  
**Head:** `qiaolei81:master` → **Base:** `KevinXie0131:master`  
**Title:** `feat: rewrite Struts2/Spring/Hibernate system as Spring Boot 3 + React 18 with Docker deployment`  
**Additions:** 22,701 | **Deletions:** 23

---

## Commits Included (16 ahead of upstream)

| SHA | Message |
|---|---|
| edfeb25a | chore: commit hive operational artifacts through t35 final delivery sign-off |
| 7451a7f9 | chore: commit hive operational artifacts through t33 final acceptance |
| e33a4f5d | test: strengthen search-filter contract tests (t28 MISMATCH-1) |
| 260ecfb0 | chore: add Docker deployment artifacts |
| 3332f8c3 | fix: correct JDBC URL folding and nginx actuator proxy path |
| a2a5939b | fix: make GET /health public (no JWT required) for liveness probes |
| f19e4ec1 | test(t28): prove MISMATCH-1 fixed — add ?name= search tests for all 4 list endpoints |
| 09d9c99d | fix: align search @RequestParam to accept 'name' query param in all list controllers |
| 1430d0c4 | docs(t21): update contract test comments — MISMATCH-2/3 resolved, BUG-1/2 fixed |
| 2f071d80 | fix(db): V3 migration — rename role 'Administrator' to 'ADMIN' for RBAC |
| f78043a3 | chore: add .gitignore and task artifacts; remove tracked build artifacts |
| 93174262 | fix(t20): fix 22 contract tests + 7 PM scenario tests (all 55 pass) |
| bd7af823 | chore(t16): write backend artifact and team notifications |
| edca5d41 | feat(t16): implement complete backend feature layer |
| ebf5b2df | Add unit tests for utility classes (Encrypt, JacksonJsonUtil, ClobUtil, IpUtil, ExcelExport) |
| 246eae1f | fix: strip HTML from cmanual field; add DB migration |

---

## PR Summary Provided to Upstream

The PR body includes:
- Full rewrite overview (Spring Boot 3.4.3 / Java 21, React 18 + Vite + Ant Design)
- Backend and frontend module inventory (10 controllers, 10 pages)
- Docker deployment summary (3-container stack)
- Test coverage table (110 tests, all passing)
- Sign-off lineage (t22 → t26 → t29 → t33 → t35 → v1.0.0)
- Key architecture decisions (UUID PKs, JWT, Flyway, UPPERCASE role names)
- Pre-deploy checklist (rotate secrets before user-facing deployment)
- Quick-start instructions (`docker compose up -d`)
- Link to v1.0.0 release

---

## Hand-Off to PM (t39)

The PR is live and open for upstream maintainer review at:  
**https://github.com/KevinXie0131/Struts2-Spring-Hibernate/pull/1**

The upstream maintainer (`KevinXie0131`) can now review, merge, or request changes.  
PM should confirm the PR is visible and close out the project.
