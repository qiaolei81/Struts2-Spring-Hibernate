# t56 — Nginx Security Hardening: CSP + Auth Rate Limiting

**Task:** t56 | **Role:** devops | **Status:** Complete  
**Commit:** `70b05eff` — `security: add CSP header and auth rate limiting to Nginx`  
**File changed:** `frontend/nginx.conf` (+47 lines, 1 file)

---

## Changes Made

### 1. Auth Endpoint Rate Limiting

**Added `limit_req_zone` at the top of `nginx.conf`** (before the `server {}` block). Because this file is included inside the default `http {}` block of `nginx:1.27-alpine`, the directive is correctly parsed at http context level.

```nginx
limit_req_zone $binary_remote_addr zone=auth_limit:10m rate=5r/m;
```

**Added a dedicated regex location block** that matches `/api/auth/login`, `/api/auth/register`, and `/api/auth/refresh` — more specific than the existing `location /api/` prefix block, so nginx matches it first:

```nginx
location ~ ^/api/auth/(login|register|refresh) {
    limit_req        zone=auth_limit burst=5 nodelay;
    limit_req_status 429;
    # proxy_pass → backend (same as /api/ block, shorter timeouts for auth)
    proxy_read_timeout 30s;
    ...
}
```

**Behaviour:**
- Each client IP is allowed 5 requests/minute to auth endpoints
- A burst of 5 additional requests is permitted (absorbed immediately, `nodelay`)
- Once burst is exhausted, the next request receives **HTTP 429 Too Many Requests** immediately — no queuing delay
- Zone memory: 10 MB ≈ 160,000 tracked IPs

### 2. Content-Security-Policy Header

**Added to the server-level security headers block**, inheriting to all locations that don't override `add_header`:

```nginx
add_header Content-Security-Policy
    "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';
     img-src 'self' data: blob:; font-src 'self' data:; connect-src 'self';
     frame-ancestors 'self'; object-src 'none'; base-uri 'self'; form-action 'self'"
    always;
```

**Rationale per directive:**

| Directive | Value | Why |
|---|---|---|
| `default-src` | `'self'` | Restrictive baseline |
| `script-src` | `'self'` | Vite builds hashed JS bundles; no inline scripts |
| `style-src` | `'self' 'unsafe-inline'` | Ant Design v5 (`@ant-design/cssinjs`) injects CSS-in-JS via `<style>` tags at runtime |
| `img-src` | `'self' data: blob:` | Ant Design icons use `data:` URIs; `blob:` for file preview |
| `font-src` | `'self' data:` | Ant Design embeds fonts as data URIs |
| `connect-src` | `'self'` | All XHR/fetch goes to same origin via Nginx `/api/` proxy |
| `frame-ancestors` | `'self'` | Supersedes `X-Frame-Options SAMEORIGIN` in modern browsers |
| `object-src` | `'none'` | Disable Flash / plugins entirely |
| `base-uri` | `'self'` | Prevent `<base>` tag injection attacks |
| `form-action` | `'self'` | Prevent form hijacking to external sites |

### 3. Permissions-Policy Header (bonus)

```nginx
add_header Permissions-Policy "camera=(), microphone=(), geolocation=()" always;
```

Disables browser sensor/media APIs not used by this application.

---

## Validation

Nginx config syntax validated via the exact target container:

```
docker run --rm -v frontend/nginx.conf:/etc/nginx/conf.d/default.conf:ro \
  nginx:1.27-alpine nginx -t
# → nginx: configuration file /etc/nginx/nginx.conf syntax is ok
# → nginx: configuration file /etc/nginx/nginx.conf test is successful
```

---

## Nginx Location Matching — Why the Auth Block Takes Priority

Nginx evaluates locations in this order:
1. Exact match `=`
2. Longest prefix with `^~`
3. **Regex `~` / `~*` in declaration order** ← auth block matches here
4. Longest prefix (no modifier)

`location ~ ^/api/auth/(login|register|refresh)` is a regex block, so it takes precedence over `location /api/` (a prefix block). The rate-limiting applies only to auth endpoints; all other `/api/*` traffic goes through the unthrottled proxy block.

---

## No Nginx `add_header` Inheritance Concern

Nginx's known `add_header` inheritance rule: a child location with its own `add_header` replaces the parent's headers entirely. In this config:

- `/api/auth/*` — no own `add_header` → **inherits** server-level CSP ✅  
- `/api/` — no own `add_header` → **inherits** server-level CSP ✅  
- `location /` — no own `add_header` → **inherits** server-level CSP ✅ (serves `index.html`)  
- Static assets `~* \.(js|css|...)` — has `Cache-Control` → **does NOT inherit** CSP  

The absence of CSP on static asset responses is intentional and correct: CSP is a policy that browsers enforce on the HTML document; sending it on JS/CSS files is redundant and can cause confusion.

---

## Operational Notes

- **Rate limit tuning:** 5 req/min is appropriate for login brute-force protection. If legitimate users (e.g., mobile app with token auto-refresh) need higher refresh rates, increase `rate` for `refresh` specifically by splitting into two zones.
- **Behind a load balancer:** If Nginx is behind a proxy (e.g., AWS ALB), `$binary_remote_addr` will be the load balancer IP, defeating per-client limiting. In that case, change the zone key to `$http_x_forwarded_for` and configure `set_real_ip_from` for trusted proxy CIDRs.
- **CSP `unsafe-inline` for styles:** This is the minimum required by Ant Design v5. If a future upgrade provides a nonce-based CSP mode, remove `'unsafe-inline'` and inject a nonce. Until then, the style directive is as tight as possible without breaking the UI.

---

*Completed by Devops Agent | Task t56 | 2026-03-13*
