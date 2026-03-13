# t40 Backend Artifact — Credential Externalization

**Task:** t40 | **Role:** backend | **Status:** ✅ COMPLETE

---

## Scope Assessment

[scope] Externalize test credentials from 1 test file to 1 properties file (files: 3, subsystems: 1)

---

## What Was Done

### Problem
`FeatureApiContractIntegrationTest.java` contained hardcoded `admin` / `admin123` literals
in JSON request bodies sent to `POST /auth/login`, triggering GitGuardian's
"Username Password" scanner on PR #1.

### Solution

Three files were changed across two commits:

#### Commit `0b3e467c` — `refactor(tests): externalise test credentials to application-test.yml`
1. **`backend/src/main/resources/application-test.yml`** — added `test.seed` block:
   ```yaml
   test:
     seed:
       admin-username: admin
       admin-password: admin123
   ```
2. **`backend/src/test/java/com/rml/system/integration/FeatureApiContractIntegrationTest.java`**:
   - Added `import org.springframework.beans.factory.annotation.Value`
   - Added `@Value("${test.seed.admin-username}") private String adminUsername`
   - Added `@Value("${test.seed.admin-password}") private String adminPassword`
   - Replaced all three hardcoded JSON credential bodies with string-concatenated fields
   - Updated Javadoc comment to reference application-test.yml

#### Commit `214d3d35` — `refactor(tests): remove password literal from test-seed.sql comment`
3. **`backend/src/test/resources/test-seed.sql`** — removed inline `admin123` from comment,
   replaced with pointer to `application-test.yml`.

---

## Key Paths (confirmed match)

| Location | Key |
|---|---|
| `application-test.yml` | `test.seed.admin-username` / `test.seed.admin-password` |
| `FeatureApiContractIntegrationTest.java` | `${test.seed.admin-username}` / `${test.seed.admin-password}` |

Both use `test.seed.*` — injection will succeed at runtime.

---

## Remaining `"admin"` occurrences (not credential pairs — safe)

All remaining `"admin"` literals in the Java source are:
- `@WithMockUser(username = "admin", roles = {"ADMIN"})` — Spring Security annotation, no password
- `.param("name", "admin")` — a search filter query parameter
- `.value("admin")` — a JSONPath assertion

None appear alongside a password literal in a JSON body. GitGuardian scans for
username+password patterns together, so these will not trigger alerts.

---

## API / Contract Changes

None. This is a pure test-internals refactor. No production endpoints changed.

---

## Test Results

```
Tests run: 63, Failures: 0, Errors: 0, Skipped: 0  ✅
BUILD SUCCESS
```

---

## Risk Assessment

**Low.** Only test infrastructure changed. The YAML property values are deliberately
benign test fixtures (not production secrets), documented with a comment explaining
this context. The property path is within the `test` profile, never loaded in
`application.yml` (production).

---

[notify:tester] t40 complete. Credentials externalized and all 63 tests pass. No regressions. Ready for t42 full-suite verification run.
