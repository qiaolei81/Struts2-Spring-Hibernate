# t17 Frontend Artifact — OnlineUsers Page + Dynamic Sidebar Menu

## Status: COMPLETE ✅

Build: passing (`npm run build` — 0 TS errors, 0 build errors)

---

## Deliverables

### 1. Dynamic Sidebar Menu (Sidebar.tsx) — already implemented prior to t17

`Sidebar.tsx` dynamically loads the navigation tree from `GET /api/menus/tree` on mount.
- Builds nested Ant Design `Menu` items from the API tree response
- Sorts by `seq` field at each level
- Expands all top-level groups by default
- Falls back to a hardcoded static menu if the API call fails (e.g. backend not ready)
- On menu item click, calls `openTab({ key, label, path })` in the app store and navigates

**No changes were needed to Sidebar.tsx — it was already correctly implemented.**

---

### 2. OnlineUsers Page (`/online-users`) — fully implemented

Full implementation replacing the stub.

**Features:**
- Fetches `GET /api/online` → `{total, rows: OnlineUserDto[]}` on mount
- Auto-refreshes every 30 seconds (interval cleared on unmount)
- Manual Refresh button (shows loading spinner while in-flight)
- Last-refreshed timestamp displayed in header
- Online count badge (green) next to page title
- Error alert with dismiss if the API call fails
- Footer note explaining the 30-minute activity window

**Columns:**
| Column | Details |
|--------|---------|
| # | Row index |
| Username | Green tag with user icon |
| IP Address | Monospace code style |
| Login Time | Formatted `MM-DD HH:mm:ss` + tooltip showing full ISO timestamp |

---

## Files Modified

| File | Change |
|------|--------|
| `frontend/src/pages/OnlineUsers.tsx` | Full implementation (replaced stub) |
| `frontend/src/types/index.ts` | Added `SimpleListResult<T>` interface |
| `frontend/src/api/modules.ts` | Fixed endpoint `/online-users` → `/online`; updated return type to `SimpleListResult<OnlineUserDto>` |
| `frontend/src/pages/Dashboard.tsx` | Fixed `onlineApi.list()` consumer to unwrap `.rows` from the updated return type |

---

## API Contract Consumed

| Method | Endpoint | Returns |
|--------|----------|---------|
| GET | `/api/online` | `{ total: number, rows: OnlineUserDto[] }` |
| GET | `/api/menus/tree` | `MenuDto[]` (tree) — consumed by Sidebar |

---

## Contract Mismatch Fixed

The previous `onlineApi.list()` called `/api/online-users` but the architecture spec (t3-architect §3.10) defines the endpoint as `GET /api/online`. This has been corrected.

---

## Fully Wired User Paths

| Path | Status |
|------|--------|
| `/online-users` — view currently active users | ✅ |
| `/online-users` — auto-refresh every 30s | ✅ |
| `/online-users` — manual refresh | ✅ |
| Sidebar loads menu tree dynamically from API | ✅ |
| Sidebar falls back to static menu if API unavailable | ✅ |

---

## Pages Fully Wired (all tasks)

| Page | Route | Status |
|------|-------|--------|
| Login | `/login` | ✅ |
| Dashboard | `/dashboard` | ✅ |
| User Management | `/users` | ✅ |
| Role Management | `/roles` | ✅ |
| Authority Management | `/authorities` | ✅ |
| Equipment Management | `/equipment` | ✅ |
| Document Management | `/documents` | ✅ |
| Access Log Viewer | `/logs` | ✅ |
| User Stats Charts | `/stats/users` | ✅ |
| **Online Users** | `/online-users` | ✅ t17 |

---

## Backend Contracts Required

All endpoints are now implemented or pending t16 (backend):
- `GET /api/online` — must return `{ total: number, rows: OnlineUserDto[] }`
- `GET /api/menus/tree` — must return `MenuDto[]` tree (recursive `children` arrays)
