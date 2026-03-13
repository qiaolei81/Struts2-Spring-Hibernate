# Devops Execution Log

_This file will contain execution history for the devops agent._

---

## [2026-03-13 11:33:23] t31: Production deployment: write Docker Compose, Dockerfile for backend and frontend, Nginx reverse proxy config, and environment variable documentation
Production deploy: 3-container stack (MySQL 8.0 → Spring Boot → Nginx). Backend JDBC URL must use `db:3306` (Docker service name). Nginx `resolver 127.0.0.11` required for Docker DNS. All Spring Boot properties overridable via SCREAMING_SNAKE_CASE env vars. Pre-production: rotate JWT_SECRET, MYSQL_PASSWORD, set CORS_ALLOWED_ORIGINS, remove db port binding.
