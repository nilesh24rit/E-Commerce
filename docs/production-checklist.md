# CommerceX — Production Readiness Checklist

> Use this checklist before every production deployment.
> All items must be ✅ before go-live.

---

## 🔒 Security

- [ ] JWT secret is a strong random value (minimum 256-bit, base64-encoded)
  - Generate: `openssl rand -hex 32`
- [ ] JWT secret is stored in AWS Secrets Manager (NOT plaintext env var)
- [ ] JWT access token expiry is short (≤ 15 minutes recommended)
- [ ] Refresh tokens are rotated on use and stored hashed
- [ ] Passwords are hashed with BCrypt (strength ≥ 10)
- [ ] HTTPS enforced — HTTP redirects to HTTPS
- [ ] CORS restricted to known frontend origins (no `*` in production)
- [ ] HTTP security headers enabled: HSTS, X-Frame-Options, X-Content-Type-Options
- [ ] CSRF disabled (stateless JWT API — correct)
- [ ] Rate limiting enabled for auth endpoints
- [ ] No stack traces in API error responses
- [ ] No sensitive data (passwords, tokens, credentials) in logs
- [ ] Actuator endpoints restricted — only `/health` and `/info` public
- [ ] Swagger UI restricted or disabled in production
- [ ] Input validation on all request bodies (@Valid annotations)
- [ ] SQL injection protected (JPA parameterized queries — correct)
- [ ] RoleNotFoundException and proper RBAC enforced via @PreAuthorize

---

## 🗄️ Database

- [ ] AWS RDS Multi-AZ enabled for high availability
- [ ] RDS is in private subnet (not publicly accessible)
- [ ] Database credentials stored in AWS Secrets Manager
- [ ] HikariCP connection pool configured:
  - Max pool size ≥ 10
  - Min idle ≥ 2
  - Connection timeout set
  - Leak detection threshold set
- [ ] Flyway enabled in production (ddl-auto=validate)
- [ ] Flyway baseline migration applied on first deployment
- [ ] Database backups enabled (automated, retention ≥ 7 days)
- [ ] open-in-view=false (prevents connection held through view rendering)
- [ ] All critical tables have appropriate indexes

---

## 📦 Redis

- [ ] ElastiCache in private subnet
- [ ] Redis AUTH token enabled in production
- [ ] Redis password stored in AWS Secrets Manager
- [ ] Connection pool (Lettuce) configured with max-active/max-idle
- [ ] Redis cache TTLs reviewed (products: 10m, categories: 30m)
- [ ] Rate limiting uses Redis (fail-open on Redis unavailability)

---

## 📨 Kafka

- [ ] Kafka/MSK in private subnet
- [ ] SASL_SSL security protocol configured for MSK
- [ ] JAAS config stored securely (Secrets Manager or env var)
- [ ] Consumer group ID configured
- [ ] Producer retries configured
- [ ] Kafka unavailability does not crash application (async events)

---

## 🐳 Docker

- [ ] Docker image uses non-root user (`commercex`)
- [ ] Multi-stage build used (builder + runtime)
- [ ] JVM flags configured: G1GC, MaxRAMPercentage, ExitOnOutOfMemoryError
- [ ] HEALTHCHECK defined in Dockerfile
- [ ] Image pushed to ECR with versioned tag (not just `latest`)
- [ ] Container resource limits set in ECS task definition
- [ ] Graceful shutdown configured (server.shutdown=graceful)

---

## 🔄 CI/CD

- [ ] GitHub Actions CI pipeline passes
- [ ] `mvn clean test` succeeds
- [ ] `mvn clean package` produces valid JAR
- [ ] `docker build` succeeds from CI
- [ ] Docker image pushed to ECR automatically on merge to main
- [ ] ECS service updated with new task definition after push
- [ ] Health check verified after every deployment

---

## 📊 Monitoring & Logging

- [ ] CloudWatch log group created with 30-day retention
- [ ] ECS task logs shipped to CloudWatch
- [ ] Actuator `/actuator/prometheus` endpoint accessible
- [ ] Prometheus scraping CommerceX metrics
- [ ] Grafana dashboard provisioned and working
- [ ] CloudWatch alarms configured:
  - ECS CPU > 80% for 5 minutes
  - ECS Memory > 85%
  - ALB 5xx error rate > 1%
  - RDS CPU > 80%
- [ ] Log format includes correlationId for request tracing
- [ ] Business metrics tracked: orders, payments, searches, cart ops

---

## 🏥 Health Checks

- [ ] `/actuator/health` returns HTTP 200 with `status: UP`
- [ ] Health check includes: db, redis
- [ ] ECS task definition has health check configured
- [ ] ALB target group health check configured (path: /actuator/health)
- [ ] Health check grace period sufficient (≥ 60 seconds for JVM warmup)
- [ ] Readiness/liveness correctly reflect application state

---

## 🔑 Secrets Management

- [ ] NO hardcoded credentials anywhere in code or config files
- [ ] `.env` file is in `.gitignore`
- [ ] JWT secret in Secrets Manager
- [ ] Database password in Secrets Manager
- [ ] Redis auth token in Secrets Manager
- [ ] Mail password in Secrets Manager
- [ ] ECS task role has permission to read Secrets Manager secrets
- [ ] Secrets rotated on a schedule

---

## 🌐 API Security

- [ ] All customer endpoints require JWT (ROLE_CUSTOMER)
- [ ] Customers can only access their own: orders, payments, cart, wishlist, reviews
- [ ] Admin endpoints require ROLE_ADMIN
- [ ] No customer can access admin operations
- [ ] @PreAuthorize annotations verified on all sensitive endpoints
- [ ] Rate limiting active: login, register, refresh, password-reset
- [ ] 429 Too Many Requests returned correctly
- [ ] Error responses do not expose internal implementation details

---

## ☁️ AWS Infrastructure

- [ ] VPC with public/private subnets
- [ ] Security groups follow least-privilege principle
- [ ] ECS Fargate (serverless, no EC2 management)
- [ ] ALB with HTTPS listener and ACM certificate
- [ ] Route 53 DNS record pointing to ALB
- [ ] RDS Multi-AZ PostgreSQL in private subnet
- [ ] ElastiCache Redis in private subnet
- [ ] CloudWatch log group with retention policy
- [ ] ECS service with desired count ≥ 2 for HA
- [ ] Auto-scaling policy configured
- [ ] IAM roles with minimal required permissions

---

## 💾 Backup & Recovery

- [ ] RDS automated backups enabled (retention ≥ 7 days)
- [ ] RDS manual snapshot taken before major deployments
- [ ] ElastiCache backup enabled
- [ ] Recovery procedure documented and tested
- [ ] RTO/RPO objectives defined

---

## 📋 Pre-Deployment Final Check

```bash
# 1. Run tests
mvn clean test

# 2. Build package
mvn clean package -DskipTests

# 3. Validate docker-compose
docker compose config

# 4. Build docker image
docker build -t commercex:latest .

# 5. Validate actuator health locally
docker compose up -d
curl http://localhost:8080/actuator/health

# 6. Push to ECR
docker tag commercex:latest <ECR_REGISTRY>/commercex:<VERSION>
docker push <ECR_REGISTRY>/commercex:<VERSION>

# 7. Update ECS service
aws ecs update-service --cluster commercex-cluster --service commercex-service --force-new-deployment
```
