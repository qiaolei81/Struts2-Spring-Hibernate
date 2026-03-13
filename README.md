# Struts2-Spring-Hibernate System — Spring Boot + React Rewrite

This repository contains a full rewrite of the original Struts2/Spring/Hibernate system
using a modern stack: **Spring Boot 3 + React + MySQL**, deployable with Docker Compose.

---

## Stack

| Layer    | Technology                                   |
|----------|----------------------------------------------|
| Backend  | Spring Boot 3.4.3 · Java 21 · Spring Security JWT · Flyway |
| Frontend | React 18 · Vite · Ant Design                 |
| Database | MySQL 8.0 (schema managed by Flyway V1–V3)   |
| Proxy    | Nginx (reverse proxy + static asset serving) |
| Build    | Maven 3.9 · Node 20 (multi-stage Docker)     |

---

## Quick Start (Docker Compose)

**Prerequisites:** Docker 24+ and Docker Compose v2.

```bash
# 1. Clone
git clone https://github.com/qiaolei81/Struts2-Spring-Hibernate.git
cd Struts2-Spring-Hibernate

# 2. Create your env file and fill in secrets
cp .env.example .env
# Edit .env — set MYSQL_ROOT_PASSWORD, MYSQL_PASSWORD, JWT_SECRET

# 3. Build and start
docker compose up -d --build

# 4. Open in browser
open http://localhost          # default credentials: admin / admin123
```

The stack starts three containers: `system-db` → `system-backend` → `system-frontend`.
Flyway migrations run automatically on first backend startup.

---

## Environment Variables

All runtime configuration lives in `.env` (never committed — see `.gitignore`).
Use `.env.example` as the canonical reference:

| Variable             | Required | Description                              |
|----------------------|----------|------------------------------------------|
| `MYSQL_ROOT_PASSWORD`| ✅       | MySQL root password                      |
| `MYSQL_PASSWORD`     | ✅       | Application DB user password             |
| `JWT_SECRET`         | ✅       | HS512 signing secret (≥ 32 chars)        |
| `MYSQL_DATABASE`     |          | Database name (default: `system_db`)     |
| `MYSQL_USER`         |          | App DB username (default: `appuser`)     |
| `JWT_EXPIRATION_MS`  |          | Token lifetime ms (default: 86400000)    |
| `CORS_ALLOWED_ORIGINS`|         | Allowed origins (default: `http://localhost`) |
| `HTTP_PORT`          |          | Host port for Nginx (default: `80`)      |

---

## System Features

* User, role, and authority management with RBAC
* Equipment and document management (CRUD)
* Excel export of equipment list (Apache POI)
* AOP-based access logging
* User statistics charts (JSON data, client-side rendering)
* Online user session tracking
* JWT authentication with Spring Security

---

## Development

```bash
# Backend (requires Java 21 + Maven)
cd backend
mvn spring-boot:run

# Frontend (requires Node 20)
cd frontend
npm install
npm run dev
```

Backend runs on `http://localhost:8080`, frontend dev server on `http://localhost:5173`.
Set `VITE_API_BASE_URL=http://localhost:8080/api` in `frontend/.env.local` for local dev.

---

## Original System

The `src/` directory contains the original Struts2.3 + Spring3 + Hibernate4 source for reference.

Original stack: JDK7 · Eclipse MyEclipse · Oracle 10g · Tomcat 7 · jQuery EasyUI 1.3.1

---

## About

* Original project: [ZhibingXie on GitHub](https://github.com/ZhibingXie)
* Rewrite: Spring Boot + React modernisation