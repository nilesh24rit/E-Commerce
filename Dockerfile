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

# Create non-root system user and group for security
RUN addgroup -S commercex && adduser -S commercex -G commercex

# Copy executable jar from build stage
COPY --from=builder /build/target/commercex-0.0.1-SNAPSHOT.jar app.jar
RUN chown -R commercex:commercex /app

# Switch to non-root user
USER commercex

# Expose standard application port
EXPOSE 8080

# Production JVM Flags
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Container entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
