# CommerceX - Enterprise E-Commerce Backend

CommerceX is a high-performance, enterprise-grade e-commerce backend built with Spring Boot 3, PostgreSQL, Redis, Apache Kafka, Spring Security (JWT), Spring Boot Actuator, Prometheus, Grafana, and Docker Compose.

---

## 🏗️ Architecture Diagram

```text
                               +------------------------+
                               |     Client / User      |
                               +-----------+------------+
                                           |
                                           v
                               +------------------------+
                               | CommerceX Spring Boot  |
                               |    REST API (8080)     |
                               +-----+------+-----+-----+
                                     |      |     |
              +----------------------+      |     +----------------------+
              |                             |                            |
              v                             v                            v
   +--------------------+        +--------------------+        +--------------------+
   |  PostgreSQL (5432) |        |    Redis (6379)    |        |    Kafka (9092)    |
   | Database Storage   |        | Distributed Cache  |        | Event Streaming    |
   +--------------------+        +--------------------+        +--------------------+
                                            |
                                            v
                               +------------------------+
                               |  Prometheus (9090)     |
                               | Metrics Collection     |
                               +-----------+------------+
                                           |
                                           v
                               +------------------------+
                               |    Grafana (3000)      |
                               | Visual Dashboard       |
                               +------------------------+
```

---

## 🚀 Prerequisites

- **Java JDK**: 17 or higher
- **Maven**: 3.8+
- **Docker Engine**: 24.0+
- **Docker Compose**: 2.20+

---

## ⚙️ Environment Configuration

1. Copy `.env.example` to create your local `.env` file:
   ```bash
   cp .env.example .env
   ```
2. Adjust environment parameters in `.env` if necessary.

---

## 🛠️ Build & Run Commands

### 1. Build Local Package
```bash
# Compile and test
mvn clean test

# Build executable JAR
mvn clean package -DskipTests
```

### 2. Docker Image Build
```bash
# Build multi-stage Docker image
docker build -t commercex:latest .
```

### 3. Run Entire Stack with Docker Compose
```bash
# Validate compose file syntax
docker compose config

# Start all containers in background
docker compose up -d

# View application logs
docker compose logs -f commercex

# Stop and remove containers
docker compose down
```

---

## 📌 Endpoint Directory

| Service | Port | Endpoint URL | Description |
| :--- | :--- | :--- | :--- |
| **CommerceX Application** | `8080` | `http://localhost:8080` | Core REST API Server |
| **Swagger UI** | `8080` | `http://localhost:8080/swagger-ui.html` | OpenAPI Interactive Documentation |
| **OpenAPI Specs** | `8080` | `http://localhost:8080/v3/api-docs` | Raw OpenAPI v3 JSON Schema |
| **Actuator Health** | `8080` | `http://localhost:8080/actuator/health` | System & Service Health Status |
| **Actuator Prometheus**| `8080` | `http://localhost:8080/actuator/prometheus` | Prometheus Scrape Metrics |
| **Prometheus Server** | `9090` | `http://localhost:9090` | Prometheus Metrics Server |
| **Grafana Dashboard** | `3000` | `http://localhost:3000` | Observability Dashboards (`admin`/`admin`) |

---

## 📊 Observability & Metrics

CommerceX includes Spring Boot Actuator and Micrometer Prometheus metrics tracking:
- **HTTP Request Rate & Latency**: `http_server_requests_seconds_count`, `http_server_requests_seconds_sum`
- **JVM Heap Memory & CPU**: `jvm_memory_used_bytes`, `system_cpu_usage`
- **Active Live Threads**: `jvm_threads_live_threads`
- **HikariCP Database Connections**: `hikaricp_connections_active`

Grafana automatically provisions the Prometheus datasource and loads the pre-configured `CommerceX Observability Dashboard`.

---

## 🔄 CI/CD Workflow

The GitHub Actions workflow is located at `.github/workflows/ci.yml`.

### Pipeline Stages:
1. **Checkout**: Pulls code repository.
2. **Setup JDK 17**: Configures Java 17 environment with Maven dependency caching.
3. **Compile**: Runs `mvn compile`.
4. **Unit & Integration Tests**: Executes `mvn test`.
5. **Package**: Generates production JAR using `mvn package`.
6. **Docker Build**: Validates multi-stage Docker build.
