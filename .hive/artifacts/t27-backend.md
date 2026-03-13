# t27 Backend Artifact — Search Param Fix

**Task:** t27 | **Role:** Backend | **Status:** ✅ COMPLETE
**Commit:** `09d9c99d`

---

## Scope Assessment

[scope] Single-line `@RequestParam` name fix in 4 controllers (files: 4, subsystems: 1)

---

## What Was Implemented

Fixed the silent no-op search bug: all four list-endpoint controllers now correctly bind the `?name=` query parameter sent by the frontend.

### Change (identical in all 4 files)

```java
// BEFORE
@RequestParam(required = false) String q

// AFTER
@RequestParam(name = "name", required = false) String q
```

### Files Changed

| File | Endpoint |
|---|---|
| `UserController.java` | `GET /users` |
| `RoleController.java` | `GET /roles` |
| `EquipmentController.java` | `GET /equipment` |
| `DocumentController.java` | `GET /documents` |

No service layer changes were needed — the local variable `q` is passed unchanged to the service methods (`listUsers`, `listRoles`, `listEquipment`, `listDocuments`).

---

## API Contract Change

All four list endpoints now accept `?name=<term>` as their search/filter parameter.

**Before:** `GET /users?name=alice` → backend received `q=null` → returned all users unfiltered  
**After:** `GET /users?name=alice` → backend receives `q="alice"` → service filters by name

The query parameter name `name` was already what the frontend was sending. This is a **behavior fix**, not a breaking change — callers sending no param or sending `?name=` still get unfiltered results.

[notify:tester] t27 complete. MISMATCH-1 (search param `name` vs `q`) is fixed. All 4 list endpoints (`/users`, `/roles`, `/equipment`, `/documents`) now correctly bind `?name=`. 55/55 tests pass. Commit: `09d9c99d`. Please re-run t28 verification.

[notify:frontend] No frontend changes needed. The `?name=` param you were already sending now works correctly on all four list endpoints.

---

## Test Results

**55/55 tests pass** (0 failures, 0 skipped) — same count as before; no tests were broken.

Note: existing integration tests do not specifically cover the `?name=` search filter path. t28 (tester) should add or verify a search-filter test to confirm the fix end-to-end.

---

## Risk Assessment

**Low.** The change is the minimal possible fix:
- No service logic changed
- No DB queries changed
- Backward-compatible: callers omitting `?name=` continue to get all records
- The only risk would be if a caller was deliberately passing `?q=` — but frontend code uses `?name=` exclusively, so no regression.

---

## What Still Looks Risky

1. **No dedicated search test exists** in the suite — the fix is proven by compilation and behavioral reasoning, but not by an automated assertion. t28 should cover this.
2. **Service-layer `null` vs blank handling** — if a caller passes `?name=` (empty string), the service receives `""` not `null`. This was pre-existing behavior and unchanged.
