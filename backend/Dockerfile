# ============================================================
# Backend Dockerfile — Spring Boot 3.4.3 / Java 21
# Multi-stage: Maven build → Eclipse Temurin 21 JRE (Alpine)
# ============================================================

# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

# Copy POM first for dependency layer caching
COPY pom.xml .

# Pre-fetch dependencies (layer is cached as long as pom.xml doesn't change)
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -q

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Non-root security principal
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the fat JAR produced by spring-boot-maven-plugin
COPY --from=build /workspace/target/system-backend-*.jar app.jar

# Upload directory (ADR-7: files outside webapp root)
RUN mkdir -p /var/app/uploads && chown appuser:appgroup /var/app/uploads

USER appuser

EXPOSE 8080

# JAVA_OPTS env var allows heap/GC tuning at runtime without rebuilding
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
