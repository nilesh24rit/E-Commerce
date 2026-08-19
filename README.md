<div align="center">

# CommerceX

### Enterprise E-Commerce Backend Platform

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-6DB33F?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=flat-square&logo=redis)](https://redis.io/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.7-231F20?style=flat-square&logo=apachekafka)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)](https://docs.docker.com/compose/)
[![AWS](https://img.shields.io/badge/AWS-ECS%20Ready-FF9900?style=flat-square&logo=amazonaws)](https://aws.amazon.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](LICENSE)

**Production-ready REST API for e-commerce — built for scale, security, and observability.**

</div>

<p align="center">
  <a href="https://github.com/nilesh24rit/E-Commerce/issues"><img src="https://img.shields.io/badge/Report-Bug-red?style=flat-square&logo=github" alt="Report Bug"></a>
  <a href="https://github.com/nilesh24rit/E-Commerce/issues"><img src="https://img.shields.io/badge/Request-Feature-blue?style=flat-square&logo=github" alt="Request Feature"></a>
  <a href="https://github.com/nilesh24rit/E-Commerce/stargazers"><img src="https://img.shields.io/badge/⭐-Star%20this%20repo-yellow?style=flat-square" alt="Star"></a>
  <a href="https://github.com/nilesh24rit/E-Commerce/fork"><img src="https://img.shields.io/badge/🍴-Fork-lightgrey?style=flat-square" alt="Fork"></a>
</p>

---

## 📋 Table of Contents

1. [Project Overview](#1-project-overview)
2. [Key Features](#2-key-features)
3. [Architecture](#3-architecture)
4. [Tech Stack](#4-tech-stack)
5. [Project Structure](#5-project-structure)
6. [Authentication & Security](#6-authentication--security)
7. [Product Management](#7-product-management)
8. [Inventory Management](#8-inventory-management)
9. [Cart](#9-cart)
10. [Order Management](#10-order-management)
11. [Payment Processing](#11-payment-processing)
12. [Coupon Engine](#12-coupon-engine)
13. [Wishlist](#13-wishlist)
14. [Reviews & Ratings](#14-reviews--ratings)
15. [Advanced Search](#15-advanced-search)
16. [Redis Caching](#16-redis-caching)
17. [Async Processing](#17-async-processing)
18. [Application Events](#18-application-events)
19. [Kafka](#19-kafka)
20. [Docker](#20-docker)
21. [CI/CD](#21-cicd)
22. [Monitoring](#22-monitoring)
23. [AWS Deployment](#23-aws-deployment)
24. [Environment Variables](#24-environment-variables)
25. [Local Setup](#25-local-setup)
26. [API Documentation](#26-api-documentation)
27. [Testing](#27-testing)
28. [Production Deployment](#28-production-deployment)

---

## 1. Project Overview

CommerceX is a **production-ready, enterprise-grade e-commerce backend** built with Spring Boot 3. It provides a complete REST API for running an online store — from product browsing to payment processing — with full observability, event-driven architecture, and AWS cloud deployment support.

**Key design principles:**
- **Security-first**: JWT authentication, RBAC, rate limiting, HTTP security headers
- **Event-driven**: Spring Application Events + Apache Kafka for async processing
- **Observable**: Actuator, Prometheus, Grafana out of the box
- **Cloud-native**: 12-factor app, Docker containers, AWS ECS ready
- **Production-hardened**: HikariCP connection pooling, Redis distributed cache, graceful shutdown, correlation IDs

---

## 2. Key Features

| Category | Features |
|----------|---------|
| **Auth** | JWT access + refresh tokens, BCrypt passwords, token rotation, logout, password reset |
| **Products** | CRUD, category hierarchy, full-text search, pagination, filtering |
| **Inventory** | Real-time stock tracking, reservation system, low-stock alerts |
| **Cart** | Persistent cart, quantity management, coupon application |
| **Orders** | Order lifecycle management, order items, status tracking |
| **Payments** | Payment processing gateway, refund support, duplicate prevention |
| **Coupons** | Percentage and fixed discounts, expiry, usage limits |
| **Wishlist** | Save products for later, move to cart |
| **Reviews** | Star ratings (1-5), text reviews, verified purchase enforcement |
| **Search** | Advanced product search with filtering, sorting, pagination |
| **Security** | Rate limiting, CORS, HSTS, security headers, actuator restriction |
| **Async** | Email notifications, order events via Kafka and Spring events |
| **Caching** | Redis distributed cache for products, categories, coupons |
| **Observability** | Prometheus metrics, Grafana dashboards, health checks |
| **AWS** | ECS Fargate, RDS, ElastiCache, CloudWatch ready |

---

## 3. Architecture

### High-Level Architecture

```mermaid
graph TB
    Client["Client App<br/>(Web / Mobile)"]
    ALB["AWS Application<br/>Load Balancer"]
    App["CommerceX<br/>Spring Boot 3<br/>:8080"]
    PG["AWS RDS<br/>PostgreSQL 15"]
    Redis["AWS ElastiCache<br/>Redis 7"]
    Kafka["Apache Kafka<br/>Event Bus"]
    Prometheus["Prometheus<br/>:9090"]
    Grafana["Grafana<br/>:3000"]
    CW["AWS CloudWatch<br/>Logs & Metrics"]

    Client --> ALB
    ALB --> App
    App --> PG
    App --> Redis
    App --> Kafka
    App -->|"/actuator/prometheus"| Prometheus
    Prometheus --> Grafana
    App --> CW
```

### Authentication Flow

<details>
<summary>Click to expand diagram</summary>

```mermaid
sequenceDiagram
    participant C as Client
    participant API as CommerceX API
    participant DB as PostgreSQL
    participant Redis as Redis

    C->>API: POST /api/auth/login {email, password}
    API->>DB: Load user by email
    DB-->>API: User + BCrypt hash
    API->>API: Verify password (BCrypt)
    API->>DB: Create RefreshToken record
    API->>Redis: Rate limit check (5 req/60s per IP)
    API-->>C: {accessToken (15min), refreshToken (24hr)}

    C->>API: GET /api/products (Bearer accessToken)
    API->>API: JWT validation + extract roles
    API-->>C: Products list

    C->>API: POST /api/auth/refresh {refreshToken}
    API->>DB: Validate + rotate refresh token
    API-->>C: New {accessToken, refreshToken}
```

</details>

<div align="right"><a href="#-table-of-contents">⬆ back to top</a></div>

### Order Flow

<details>
<summary>Click to expand diagram</summary>

```mermaid
sequenceDiagram
    participant C as Client
    participant API as CommerceX API
    participant DB as PostgreSQL
    participant Kafka as Kafka

    C->>API: POST /api/orders (create order from cart)
    API->>DB: Validate cart items
    API->>DB: Check inventory availability
    API->>DB: Reserve inventory
    API->>DB: Create Order + OrderItems
    API->>DB: Clear cart
    API->>Kafka: Publish ORDER_CREATED event
    API-->>C: Order details {orderId, status: PENDING}

    C->>API: POST /api/payments (pay for order)
    API->>DB: Create Payment record
    API->>API: Process payment via gateway
    API->>DB: Update Payment status: COMPLETED
    API->>DB: Update Order status: CONFIRMED
    API->>Kafka: Publish ORDER_CONFIRMED event
    API-->>C: Payment confirmation
```

</details>

<div align="right"><a href="#-table-of-contents">⬆ back to top</a></div>

### Payment Flow

<details>
<summary>Click to expand diagram</summary>

```mermaid
sequenceDiagram
    participant C as Client
    participant API as CommerceX API
    participant GW as Payment Gateway
    participant DB as PostgreSQL
    participant Email as Email Service

    C->>API: POST /api/payments {orderId, method}
    API->>DB: Verify order ownership
    API->>DB: Check no duplicate payment
    API->>GW: Process payment request
    GW-->>API: Payment result
    alt Payment Successful
        API->>DB: Payment(status=COMPLETED)
        API->>DB: Order(status=CONFIRMED)
        API->>Email: Send confirmation email (async)
    else Payment Failed
        API->>DB: Payment(status=FAILED, reason)
        API-->>C: 400 Payment failed
    end
    API-->>C: Payment response
```

</details>

<div align="right"><a href="#-table-of-contents">⬆ back to top</a></div>

### Event-Driven Architecture

<details>
<summary>Click to expand diagram</summary>

```mermaid
graph LR
    OrderSvc["Order Service"]
    PaySvc["Payment Service"]
    AuthSvc["Auth Service"]

    subgraph "Spring Application Events"
        OE["OrderPlacedEvent"]
        PE["PaymentCompletedEvent"]
        RE["PasswordResetEvent"]
        UE["UserRegisteredEvent"]
    end

    subgraph "Kafka Topics"
        K1["order-events"]
        K2["payment-events"]
    end

    subgraph "Consumers"
        Email["Email Service<br/>(Async @EventListener)"]
        Inv["Inventory Service<br/>(Stock reservation)"]
        KConsumer["Kafka Consumer<br/>(External systems)"]
    end

    OrderSvc --> OE --> Email
    OrderSvc --> OE --> Inv
    PaySvc --> PE --> Email
    AuthSvc --> RE --> Email
    AuthSvc --> UE --> Email
    OrderSvc --> K1 --> KConsumer
    PaySvc --> K2 --> KConsumer
```

</details>

<div align="right"><a href="#-table-of-contents">⬆ back to top</a></div>

### AWS Deployment Architecture

<details>
<summary>Click to expand diagram</summary>

```mermaid
graph TB
    Internet["Internet"]
    R53["Route 53 DNS"]
    ACM["ACM Certificate"]
    ALB["Application Load Balancer<br/>HTTPS :443"]

    subgraph VPC["VPC"]
        subgraph Public["Public Subnets"]
            ALB
        end

        subgraph Private["Private Subnets"]
            subgraph ECS["ECS Fargate Cluster"]
                T1["CommerceX Task 1"]
                T2["CommerceX Task 2"]
            end
            RDS["RDS PostgreSQL<br/>Multi-AZ"]
            Redis["ElastiCache Redis"]
            MSK["Amazon MSK<br/>Kafka (Optional)"]
        end
    end

    CW["CloudWatch<br/>Logs + Alarms"]
    ECR["Amazon ECR<br/>Container Registry"]
    SM["Secrets Manager<br/>Credentials"]

    Internet --> R53 --> ALB
    ACM --> ALB
    ALB --> T1 & T2
    T1 & T2 --> RDS
    T1 & T2 --> Redis
    T1 & T2 --> MSK
    T1 & T2 --> CW
    ECR --> T1 & T2
    SM --> T1 & T2
```

</details>

<div align="right"><a href="#-table-of-contents">⬆ back to top</a></div>

---

## 4. Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Language** | Java | 17 |
| **Framework** | Spring Boot | 3.2.4 |
| **Security** | Spring Security + JWT (jjwt) | 0.12.5 |
| **Database** | PostgreSQL | 15 |
| **ORM** | Spring Data JPA / Hibernate | 6.4 |
| **DB Migration** | Flyway | 10.x |
| **Cache** | Redis (Lettuce) | 7 |
| **Messaging** | Apache Kafka | 3.7 |
| **Mapping** | MapStruct | 1.5.5 |
| **Validation** | Jakarta Bean Validation | 3.0 |
| **Email** | Spring Mail + Thymeleaf templates | - |
| **API Docs** | Springdoc OpenAPI 3 | 2.5.0 |
| **Monitoring** | Micrometer + Prometheus + Grafana | - |
| **Build**
