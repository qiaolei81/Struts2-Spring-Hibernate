# Backend Task t55 — Execution Artifact

**Task:** Fix N+1 getRoleStats query, bulk UPDATE clearInactiveUsers, and add JWT secret startup validation  
**Status:** Complete  
**Commit:** `c5ebab80`  
**Test result:** 67 tests, 0 failures, 0 errors (4 new tests added)

---

## What Was Implemented

### 1. N+1 Fix — `getRoleStats()` (UserService + UserRepository)

**Before:** `userRepository.findAll()` loaded every user into memory, then iterated `user.getRoles()` triggering N lazy loads — O(N×M) in user×role count.

**After:** Single aggregate JPQL query replaces the whole loop:

```java
// UserRepository
@Query("SELECT r.name, COUNT(u.id) FROM User u JOIN u.roles r GROUP BY r.name")
List<Object[]> countUsersByRole();

// UserService.getRoleStats()
List<Object[]> rows = userRepository.countUsersByRole();
if (rows.isEmpty()) return List.of(new RoleStatDto("No Role", 0L));
return rows.stream()
    .map(row -> new RoleStatDto((String) row[0], (Long) row[1]))
    .collect(Collectors.toList());
```

**Impact:** 1 DB round-trip regardless of user/role count. No Hibernate lazy loads.

---

### 2. Bulk UPDATE — `clearInactiveUsers()` (SchedulingConfig + UserRepository)

**Before:** `findAll()` + per-entity `save()` — O(N) reads + O(K) writes every 5 minutes.

**After:** Single `@Modifying` bulk UPDATE:

```java
// UserRepository
@Modifying
@Transactional
@Query("UPDATE User u SET u.lastActivity = null WHERE u.lastActivity IS NOT NULL AND u.lastActivity < :threshold")
int clearActivityBefore(@Param("threshold") LocalDateTime threshold);

// SchedulingConfig
@Scheduled(fixedDelay = 300_000)
@Transactional
public void clearInactiveUsers() {
    userRepository.clearActivityBefore(LocalDateTime.now().minusMinutes(30));
}
```

**Impact:** 1 SQL statement per 5-minute tick regardless of user count.

---

### 3. JWT Secret Startup Validation (JwtTokenProvider)

**Before:** No check — application starts successfully with the shipped placeholder secret.

**After:** `@PostConstruct init()` rejects the placeholder at startup:

```java
private final String rawSecret;  // stored for validation

@PostConstruct
void init() {
    if (rawSecret.contains("replace-in-production")) {
        throw new IllegalStateException(
            "app.jwt.secret is the default placeholder value. " +
            "Set APP_JWT_SECRET (or app.jwt.secret) to a secure random key before deployment.");
    }
}
```

**Impact:** Application refuses to start if `APP_JWT_SECRET` has not been set in production. Test profile secret (`test-secret-key-for-unit-tests-only-minimum-32-characters`) does not contain the trigger string — all integration tests continue to pass.

---

## API / Contract Changes

**None.** All three fixes are internal implementation changes:
- `GET /users/stats/by-role` returns the same `List<RoleStatDto>` shape
- Scheduling behavior is unchanged (same 5-minute interval, same effect)
- JWT token format/validation is unchanged

---

## Files Changed

| File | Change |
|---|---|
| `repository/UserRepository.java` | +`countUsersByRole()` aggregate query, +`clearActivityBefore()` @Modifying query |
| `service/UserService.java` | `getRoleStats()` rewritten to use aggregate query |
| `config/SchedulingConfig.java` | `clearInactiveUsers()` rewritten to use bulk UPDATE + `@Transactional` |
| `security/JwtTokenProvider.java` | +`rawSecret` field, +`@PostConstruct init()` placeholder guard |
| `test/security/JwtTokenProviderTest.java` | +2 tests: `init_throwsForPlaceholderSecret`, `init_acceptsValidSecret` |
| `test/service/UserServiceTest.java` | New file: 2 unit tests for `getRoleStats()` aggregate path |

---

## Risk Assessment

- **Low risk overall** — all changes are drop-in replacements with identical external behaviour.
- **JWT startup guard** — developers running locally without `APP_JWT_SECRET` will get a startup failure. This is intentional (forces secret rotation). Document in README if not already covered by `.env.example`.
- **H2 compatibility** — both new JPQL queries (`COUNT ... GROUP BY` and bulk `UPDATE`) are standard JPQL and work on H2 in test mode (verified: all 67 tests pass).
- **`@Transactional` on `@Scheduled`** — Spring proxies `@Configuration` beans, so `@Transactional` on `clearInactiveUsers()` is applied correctly. The `@Modifying` repository method also carries its own `@Transactional`; the outer one propagates as `REQUIRED` (default) — harmless.

---

## Remaining Risks (Not In Scope)

- **MapStruct migration** (ADR refactoring §3.1) — still hand-written `toDto()` methods
- **`lastActivity` staleness** (§3.9) — only updated on login, not on each request
- **Tree endpoint caching** (§3.6) — menus/authorities still hit DB on every page load
