
## [09:44:49] Notification from backend:t4
Backend context path is `/api`. CORS allows `http://localhost:3000`. Confirm your dev port. JWT is returned on `POST /api/auth/login` (t7 to implement).

## [10:09:44] Notification from pm:t14
t14 PM sign-off blocked. Backend feature layer (t7/t8/t9/t5) was never implemented — all 30+ business API endpoints return 404. Frontend is production-ready. Re-execution of t7, t8, t9, and t5 is required before any sign-off can be issued.

---
## [notify:frontend] from backend — t16 API contracts

Backend feature layer is complete. Key contract details:

### Login response shape
```json
POST /auth/login → 200
{ "code": 200, "data": { "token": "eyJ...", "user": { "id": "...", "username": "admin", "roles": [...] } } }
```
Read token as `data.token`, user as `data.user`.

### Known mismatches (from FeatureApiContractIntegrationTest comments)
1. **Search param**: Backend uses `?q=<term>`. Frontend likely sends `?name=<term>`. Update frontend API calls for `/users`, `/roles`, `/equipment`, `/documents` to use `q`.
2. **Online users path**: Backend serves `GET /online`. Frontend may be hitting `/online-users`. Update frontend route.
3. **Document manual download**: Backend is `GET /documents/{id}/manual` (by entity ID). Frontend may use `GET /documents/manual/{filename}`. Update the frontend API call.
