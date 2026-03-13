# Backend Scaffold — t4 Execution Log

**Task:** t4 — Scaffold Spring Boot project in new directory with Maven structure, dependencies, and base configuration  
**Role:** Backend  
**Status:** Complete  
**Build:** ✅ `mvn test` — 8 tests, 0 failures

---

## [scope] Single Spring Boot scaffold in `backend/` (files: 18, subsystems: 1)

---

## What Was Built

### Directory: `backend/` (sibling to legacy `src/`)

```
backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/rml/system/
    │   │   ├── SystemApplication.java          # @SpringBootApplication + @EnableJpaAuditing
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java         # Spring Security + JWT + CORS + RBAC
    │   │   │   └── WebMvcConfig.java           # Upload static resource handler
    │   │   ├── controller/
    │   │   │   └── HealthController.java       # GET /health smoke-test
    │   │   ├── dto/
    │   │   │   ├── ApiResponse.java            # Uniform response envelope
    │   │   │   └── PageResponse.java           # Paginated list envelope (total + rows)
    │   │   ├── entity/
    │   │   │   └── BaseEntity.java             # createdAt / updatedAt audit fields
    │   │   ├── exception/
    │   │   │   ├── AppException.java           # Runtime exception with HttpStatus
    │   │   │   └── GlobalExceptionHandler.java # @RestControllerAdvice
    │   │   └── security/
    │   │       ├── JwtTokenProvider.java       # Token generation + validation (JJWT 0.12.x)
    │   │       ├── JwtAuthenticationFilter.java# Bearer header → SecurityContext
    │   │       ├── JwtAuthenticationEntryPoint.java # JSON 401 (no redirect)
    │   │       └── UserDetailsServiceImpl.java # Stub — replace in t7
    │   └── resources/
    │       ├── application.yml                 # Base config
    │       ├── application-test.yml            # H2 in-memory for tests
    │       └── db/migration/
    │           └── V0__baseline.sql            # Flyway baseline placeholder
    └── test/
        └── java/com/rml/system/
            ├── SystemApplicationTests.java     # Spring context load test
            └── security/
                └── JwtTokenProviderTest.java   # 7 pure-unit JWT tests
```

---

## Technology Choices

| Concern | Choice | Reason |
|---|---|---|
| Framework | Spring Boot 3.4.3 | Stable 3.x LTS; Java 17+ required |
| Java target | 21 | LTS; compatible with local JDK 25 build env |
| JWT | JJWT 0.12.7 | Latest stable; modular API |
| DB migrations | Flyway | ADR-5 |
| Password hash | BCrypt | ADR-4 |
| Sessions | Stateless JWT | ADR-9 |
| ORM | Spring Data JPA + Hibernate 6.x | ADR-10 |
| Mapping | MapStruct 1.6.3 | ADR-2 (Entity ↔ DTO) |
| Lombok | 1.18.44 | Java 25 compiler compatibility fix |
| Excel | Apache POI OOXML 5.3.0 | t8 requirement |
| DB driver | MySQL (runtime) | New system target; configurable |
| Test DB | H2 in-memory | Fast CI; Flyway disabled in test profile |

---

## API Contract

### Public endpoints (no JWT required)

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/login` | Authentication — stub, t7 implements |
| POST | `/api/auth/register` | Self-registration — stub, t7 implements |
| POST | `/api/auth/refresh` | Token refresh — stub, t7 implements |
| GET | `/api/health` | Liveness smoke-test |
| GET | `/api/actuator/health` | Spring Boot health |
| GET | `/api/actuator/info` | Build info |

All other paths require `Authorization: Bearer <token>`.

### Response envelope (all endpoints)

```json
{
  "success": true,
  "code": 200,
  "message": "OK",
  "data": { ... }
}
```

### Paginated response (list endpoints)

```json
{
  "success": true,
  "code": 200,
  "message": "OK",
  "data": {
    "total": 42,
    "rows": [ ... ]
  }
}
```

---

## ADRs Implemented

| ADR | Implemented |
|---|---|
| ADR-2: Dual-model (Entity ↔ DTO) | `BaseEntity`, `ApiResponse`, MapStruct dependency |
| ADR-3: REST path-pattern RBAC | `SecurityConfig` — `@EnableMethodSecurity`, `@PreAuthorize` hook |
| ADR-4: BCrypt | `BCryptPasswordEncoder` bean in `SecurityConfig` |
| ADR-5: Flyway migrations | `flyway-core` + `flyway-mysql` deps; `V0__baseline.sql` |
| ADR-7: Upload outside webapp root | `WebMvcConfig.addResourceHandlers` + `app.upload.base-dir` config |
| ADR-9: Stateless JWT | `SessionCreationPolicy.STATELESS`, `JwtTokenProvider` |
| ADR-10: Spring Data JPA repos | JPA starter; empty `repository/` package ready for t5/t7 |

---

## Risk Notes

1. **UserDetailsServiceImpl is a stub** — throws `UsernameNotFoundException` on every call. t7 must replace it with the JPA-backed impl before auth works end-to-end.
2. **JWT secret is a placeholder** — `application.yml` contains `your-256-bit-secret-key...`. Production deployment MUST override `app.jwt.secret` via environment variable or secrets manager.
3. **Database not yet configured** — `application.yml` points to `localhost:3306/system_db` with `validate` DDL mode. This will fail to start without a running MySQL + Flyway migrations (t5 deliverable). Use `-Dspring.profiles.active=test` for local runs before t5 lands.
4. **Flyway V0 is a placeholder** — real schema goes in `V1__create_initial_schema.sql` from t5. The baseline file avoids Flyway bootstrapping errors.
5. **CORS** — currently allows only `http://localhost:3000`. React scaffold (t6) may use a different port; update `app.cors.allowed-origins`.
6. **Java 25 / Lombok warning** — `sun.misc.Unsafe::objectFieldOffset` is deprecated but functional. Will be blocked in a future JDK release. Track Lombok 2.x for full Java 25+ support.

---

## Notifications Sent

- [notify:database] `backend/` scaffold complete. Flyway is configured at `classpath:db/migration`. Please add `V1__create_initial_schema.sql` (and subsequent migrations) in `backend/src/main/resources/db/migration/`. Table names must match what `@Entity` classes in `com.rml.system.entity` reference. UUID PKs (VARCHAR 36) per ADR-1.
- [notify:frontend] Backend context path is `/api`. CORS currently allows `http://localhost:3000`. JWT access token is returned on POST `/api/auth/login` (t7 to implement). Confirm your dev server port so I can update CORS config.
- [notify:tester] Spring Boot project lives in `backend/`. Run tests with `mvn test -Dspring.profiles.active=test` from that directory. H2 in-memory DB — no external DB needed for unit/integration tests.
