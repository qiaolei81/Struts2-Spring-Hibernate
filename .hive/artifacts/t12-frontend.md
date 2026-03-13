# t12 Frontend – User Statistics Charts & Access Log Viewer

## Status: COMPLETE ✅

## Files Modified

| File | Change |
|------|--------|
| `frontend/package.json` | Added `recharts` dependency |
| `frontend/src/pages/UserStats.tsx` | Full implementation (was stub) |
| `frontend/src/pages/LogViewer.tsx` | Full implementation (was stub) |

## Pages Fully Wired

### UserStats (`/stats/users`)
- Fetches `GET /api/users/stats/by-role` → `UserRoleStat[]`
- Renders: **Bar chart** (users per role) + **Pie chart** (role distribution with % labels) + **Summary tiles** (count cards per role)
- Library: `recharts` with `ResponsiveContainer` (responsive, no fixed pixel width)
- Handles loading/error/empty states

### LogViewer (`/logs`)
- Fetches `GET /api/logs` via `logApi.list(params)` with full pagination + search
- Reuses existing `DataTable` component
- Columns: Time (sortable, dayjs formatted), User (Tag), IP (code), Message (truncated with Tooltip)
- Search by username; Refresh button

## API Contracts Consumed

| Endpoint | Method | Returns |
|----------|--------|---------|
| `GET /api/users/stats/by-role` | `userApi.statsByRole()` | `UserRoleStat[]` |
| `GET /api/logs` | `logApi.list(PageRequest)` | `PageResult<LogDto>` |

**Note:** `logApi.list` uses `name` query param for search — this filters by username on the backend. If the backend field differs, notify backend to align.

## Dependencies Added
- `recharts` ^2.x (installed to `node_modules`, saved to `package.json`)

## TypeScript
- `npx tsc --noEmit` exits 0 — no type errors

## User Paths Now Complete
- `/stats/users` — fully interactive charts page
- `/logs` — fully interactive paginated log viewer with search

## No Contract Mismatches Found
