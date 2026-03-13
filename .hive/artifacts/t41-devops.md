# t41 DevOps Artifact — GitGuardian Suppression Config

**Task:** t41 | **Role:** DevOps | **Status:** ✅ COMPLETE  
**Commit:** fd4100b8  
**Date:** 2026-03-13

---

## Scope Assessment

[scope] Add .gitguardian.yml at repo root to suppress test fixture false-positives (files: 1, subsystems: 1)

---

## What Was Done

Created **`.gitguardian.yml`** at the repository root (committed and pushed to `qiaolei81:master`).

### File: `.gitguardian.yml`

```yaml
version: 2

secret:
  ignored_paths:
    - "backend/src/test/**"
    - "backend/src/test/resources/**"

  ignored_matches:
    - name: "test-fixture-admin-password"
      match: "admin123"
    - name: "test-fixture-testuser-password"
      match: "pass1234"
```

### Suppression Strategy

| Mechanism | Scope | Rationale |
|---|---|---|
| `ignored_paths: backend/src/test/**` | All test Java source | Integration test harness only; no production code |
| `ignored_paths: backend/src/test/resources/**` | `application-test.yml`, seed SQL | Test-only fixtures; `admin-password: admin123` is synthetic |
| `ignored_matches: admin123` | Any remaining literal occurrence | Exact value from GitGuardian alert on commit 93174262 |
| `ignored_matches: pass1234` | `testuser` creation fixture | Defensive: second test user created in `FeatureApiContractIntegrationTest` |

### Why Both Mechanisms

- **`ignored_paths`** is the primary defence: suppresses all future additions to the test tree without needing per-value maintenance.  
- **`ignored_matches`** provides a catch-all for any path that somehow escapes the glob (e.g. if a test file is moved outside `src/test/`), and also satisfies GitGuardian's SHA-pinned incident resolution on the existing PR comment.

---

## Observations / Follow-up

1. **`application-test.yml` still contains `admin-password: admin123`** at key path `test.credentials.*`, while the test class reads `${test.seed.admin-username}`. This key-path mismatch may cause `@Value` injection to fail at test runtime — t40 (backend) should reconcile `test.credentials` vs `test.seed` in `application-test.yml`.

2. **`FeatureApiContractIntegrationTest.java` line 86** still contains a literal `"admin123"` in the "missing username → 400" test body. This residual literal is now covered by `ignored_matches`, but t40 may wish to replace it with `adminPassword` field reference for consistency.

3. The `.gitguardian.yml` uses `version: 2` (current format) and is honoured by both the ggshield CLI and GitGuardian's GitHub App scanner.

---

## Git Record

| Field | Value |
|---|---|
| File added | `.gitguardian.yml` |
| Commit | `fd4100b8` |
| Branch | `master` |
| Remote pushed | `qiaolei81/Struts2-Spring-Hibernate` |
| PR #1 effect | GitGuardian scanner will re-evaluate on next push; new commit in PR should clear the alert |
