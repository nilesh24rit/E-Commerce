# ====================================================
# STAGE 1: Build Stage
# ====================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Copy pom.xml and resolve dependencies to maximize Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests -B

# ====================================================
# STAGE 2: Lightweight Production Runtime
# ====================================================
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Install wget for health check
RUN apk add --no-cache wget

# Create non-root system user and group for security
RUN addgroup -S commercex && adduser -S commercex -G commercex

# Create log directory
RUN mkdir -p /var/log/commercex && chown -R commercex:commercex /var/log/commercex

# Copy executable jar from build stage
COPY --from=builder /build/target/commercex-0.0.1-SNAPSHOT.jar app.jar
RUN chown -R commercex:commercex /app

# Switch to non-root user
USER commercex

# Expose standard application port
EXPOSE 8080

# Production JVM Flags - G1GC, bounded heap, OOME exit
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

# Health check (uses actuator)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Container entry point with graceful shutdown support
ENTRYPOINT ["sh", "-c", "java  -jar app.jar"]
