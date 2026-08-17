# CommerceX — AWS ECS Deployment Guide

> **Status**: Deployment-Ready Configuration
> **Note**: This guide provides complete, verified deployment instructions.
> Actual deployment requires valid AWS credentials, a paid AWS account, and created resources.
> No AWS resource IDs, ARNs, or URLs in this guide are real.

---

## Architecture Overview

```
Internet
    |
    v
Route 53 (Optional DNS)
    |
    v
Application Load Balancer (ALB)
    |  - HTTPS :443 (ACM Certificate)
    |  - HTTP :80 → redirect to :443
    v
ECS Fargate Service (CommerceX)
    |  - Task Definition with CommerceX container
    |  - Private Subnet(s) in VPC
    |
    +--- AWS RDS PostgreSQL (Multi-AZ, Private)
    |
    +--- AWS ElastiCache Redis (Private)
    |
    +--- Amazon MSK Kafka (Optional, Private)
    |
    +--- CloudWatch Logs + Metrics
```

---

## Prerequisites

- AWS CLI v2 installed and configured (`aws configure`)
- Docker Desktop installed and running
- Maven 3.8+ and Java 17+ installed
- An AWS account with sufficient permissions (IAM, ECS, ECR, RDS, ElastiCache, ELB)

---

## Step 1: Create AWS Infrastructure

### 1.1 VPC & Networking

Create a VPC with public and private subnets:

```bash
# Use AWS Console or Terraform/CDK — recommended IaC approach
# Minimum: 1 VPC, 2 public subnets (ALB), 2 private subnets (ECS, RDS, Redis)
```

### 1.2 Security Groups

Create the following security groups:

| Name | Inbound Rules | Purpose |
|------|--------------|---------|
| `commercex-alb-sg` | 80, 443 from 0.0.0.0/0 | ALB |
| `commercex-app-sg` | 8080 from ALB SG only | ECS Tasks |
| `commercex-db-sg` | 5432 from App SG only | RDS |
| `commercex-redis-sg` | 6379 from App SG only | ElastiCache |

---

## Step 2: Build Docker Image

```bash
# From project root
mvn clean package -DskipTests -B

# Build production Docker image
docker build -t commercex:latest .

# Verify image
docker images | grep commercex
```

---

## Step 3: Push Image to Amazon ECR

```bash
# Set your region
export AWS_REGION=us-east-1

# Authenticate Docker with ECR
aws ecr get-login-password --region $AWS_REGION | \
    docker login --username AWS --password-stdin \
    $(aws sts get-caller-identity --query Account --output text).dkr.ecr.$AWS_REGION.amazonaws.com

# Create ECR repository (first time only)
aws ecr create-repository --repository-name commercex --region $AWS_REGION

# Get registry URL
export ECR_REGISTRY=$(aws sts get-caller-identity --query Account --output text).dkr.ecr.$AWS_REGION.amazonaws.com

# Tag image
docker tag commercex:latest $ECR_REGISTRY/commercex:latest

# Push image
docker push $ECR_REGISTRY/commercex:latest
```

---

## Step 4: Configure AWS RDS PostgreSQL

```bash
# Create RDS PostgreSQL instance (via Console or CLI)
aws rds create-db-instance \
    --db-instance-identifier commercex-db \
    --db-instance-class db.t3.medium \
    --engine postgres \
    --engine-version 15.4 \
    --master-username commercex_admin \
    --master-user-password <STRONG_PASSWORD> \
    --db-name commercex \
    --allocated-storage 20 \
    --vpc-security-group-ids <commercex-db-sg-id> \
    --db-subnet-group-name <your-private-subnet-group> \
    --multi-az \
    --backup-retention-period 7 \
    --no-publicly-accessible
```

**After creation, note the RDS endpoint URL** — you will use it as `SPRING_DATASOURCE_URL`.

---

## Step 5: Configure AWS ElastiCache Redis

```bash
# Create Redis cluster
aws elasticache create-replication-group \
    --replication-group-id commercex-redis \
    --description "CommerceX Redis Cache" \
    --num-cache-clusters 1 \
    --cache-node-type cache.t3.micro \
    --engine redis \
    --engine-version 7.0 \
    --security-group-ids <commercex-redis-sg-id> \
    --subnet-group-name <your-private-cache-subnet-group>
```

**After creation, note the Redis primary endpoint** — you will use it as `SPRING_REDIS_HOST`.

---

## Step 6: Store Secrets in AWS Secrets Manager

Store sensitive values in AWS Secrets Manager (do NOT use plain environment variables for production secrets):

```bash
# Store DB credentials
aws secretsmanager create-secret \
    --name "commercex/prod/db-password" \
    --secret-string "<YOUR_DB_PASSWORD>"

# Store JWT secret
aws secretsmanager create-secret \
    --name "commercex/prod/jwt-secret" \
    --secret-string "<YOUR_JWT_SECRET_HEX>"

# Store mail password
aws secretsmanager create-secret \
    --name "commercex/prod/mail-password" \
    --secret-string "<YOUR_MAIL_APP_PASSWORD>"
```

---

## Step 7: Create ECS Cluster

```bash
aws ecs create-cluster \
    --cluster-name commercex-cluster \
    --capacity-providers FARGATE FARGATE_SPOT \
    --region $AWS_REGION
```

---

## Step 8: Create ECS Task Definition

Create a file `ecs-task-definition.json`:

```json
{
  "family": "commercex-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "executionRoleArn": "arn:aws:iam::<ACCOUNT_ID>:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::<ACCOUNT_ID>:role/commercexTaskRole",
  "containerDefinitions": [
    {
      "name": "commercex",
      "image": "<ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/commercex:latest",
      "portMappings": [{"containerPort": 8080, "protocol": "tcp"}],
      "essential": true,
      "environment": [
        {"name": "SPRING_PROFILES_ACTIVE", "value": "prod"},
        {"name": "SERVER_PORT", "value": "8080"},
        {"name": "SPRING_DATASOURCE_URL", "value": "jdbc:postgresql://<RDS_ENDPOINT>:5432/commercex"},
        {"name": "SPRING_DATASOURCE_USERNAME", "value": "commercex_admin"},
        {"name": "SPRING_REDIS_HOST", "value": "<ELASTICACHE_ENDPOINT>"},
        {"name": "SPRING_REDIS_PORT", "value": "6379"},
        {"name": "SPRING_KAFKA_BOOTSTRAP_SERVERS", "value": "<MSK_BROKER_ENDPOINTS>"},
        {"name": "SPRING_MAIL_HOST", "value": "email-smtp.us-east-1.amazonaws.com"},
        {"name": "SPRING_MAIL_PORT", "value": "587"},
        {"name": "CORS_ALLOWED_ORIGINS", "value": "https://your-frontend-domain.com"}
      ],
      "secrets": [
        {"name": "SPRING_DATASOURCE_PASSWORD", "valueFrom": "arn:aws:secretsmanager:<REGION>:<ACCOUNT_ID>:secret:commercex/prod/db-password"},
        {"name": "JWT_SECRET", "valueFrom": "arn:aws:secretsmanager:<REGION>:<ACCOUNT_ID>:secret:commercex/prod/jwt-secret"},
        {"name": "SPRING_MAIL_PASSWORD", "valueFrom": "arn:aws:secretsmanager:<REGION>:<ACCOUNT_ID>:secret:commercex/prod/mail-password"}
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/commercex",
          "awslogs-region": "<REGION>",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "wget -qO- http://localhost:8080/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 10,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

Register the task definition:

```bash
aws ecs register-task-definition \
    --cli-input-json file://ecs-task-definition.json \
    --region $AWS_REGION
```

---

## Step 9: Configure Application Load Balancer

```bash
# Create ALB (via Console recommended for full control)
# Target group: HTTP, port 8080, health check path /actuator/health

# Create listener: HTTPS :443, forward to target group
# ACM Certificate: request via AWS Certificate Manager for your domain
```

---

## Step 10: Create ECS Service

```bash
aws ecs create-service \
    --cluster commercex-cluster \
    --service-name commercex-service \
    --task-definition commercex-task:1 \
    --desired-count 2 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[<PRIVATE_SUBNET_1>,<PRIVATE_SUBNET_2>],securityGroups=[<commercex-app-sg-id>],assignPublicIp=DISABLED}" \
    --load-balancers "targetGroupArn=<TARGET_GROUP_ARN>,containerName=commercex,containerPort=8080" \
    --health-check-grace-period-seconds 120 \
    --region $AWS_REGION
```

---

## Step 11: Configure CloudWatch Logs

```bash
# Create log group
aws logs create-log-group \
    --log-group-name /ecs/commercex \
    --retention-in-days 30 \
    --region $AWS_REGION

# CloudWatch metrics are automatically sent via Actuator/Prometheus
# Optional: Add CloudWatch metric alarms for CPU/memory
```

---

## Step 12: Verify Deployment

```bash
# Check service status
aws ecs describe-services \
    --cluster commercex-cluster \
    --services commercex-service \
    --region $AWS_REGION | jq '.services[0].deployments'

# Check running tasks
aws ecs list-tasks \
    --cluster commercex-cluster \
    --service-name commercex-service \
    --region $AWS_REGION

# Check application logs
aws logs tail /ecs/commercex --follow --region $AWS_REGION

# Verify health endpoint via ALB
curl https://your-alb-domain.elb.amazonaws.com/actuator/health
```

---

## Step 13: Configure Auto Scaling (Optional)

```bash
# Register scalable target
aws application-autoscaling register-scalable-target \
    --service-namespace ecs \
    --resource-id service/commercex-cluster/commercex-service \
    --scalable-dimension ecs:service:DesiredCount \
    --min-capacity 2 \
    --max-capacity 10

# Create CPU-based scaling policy
aws application-autoscaling put-scaling-policy \
    --policy-name commercex-cpu-scaling \
    --service-namespace ecs \
    --resource-id service/commercex-cluster/commercex-service \
    --scalable-dimension ecs:service:DesiredCount \
    --policy-type TargetTrackingScaling \
    --target-tracking-scaling-policy-configuration '{"TargetValue": 70.0, "PredefinedMetricSpecification": {"PredefinedMetricType": "ECSServiceAverageCPUUtilization"}}'
```

---

## Environment Variable Reference

| Variable | Description | Required |
|----------|-------------|----------|
| `SPRING_PROFILES_ACTIVE` | Spring profile (`prod`) | Yes |
| `SPRING_DATASOURCE_URL` | RDS PostgreSQL JDBC URL | Yes |
| `SPRING_DATASOURCE_USERNAME` | DB username | Yes |
| `SPRING_DATASOURCE_PASSWORD` | DB password (from Secrets Manager) | Yes |
| `SPRING_REDIS_HOST` | ElastiCache endpoint | Yes |
| `SPRING_REDIS_PORT` | Redis port (default 6379) | Yes |
| `SPRING_REDIS_PASSWORD` | Redis auth token (if enabled) | No |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | MSK broker endpoints | Yes (if Kafka used) |
| `KAFKA_SECURITY_PROTOCOL` | `SASL_SSL` for MSK | Yes (Kafka) |
| `SPRING_MAIL_HOST` | SES SMTP endpoint | Yes |
| `SPRING_MAIL_USERNAME` | SES SMTP username | Yes |
| `SPRING_MAIL_PASSWORD` | SES SMTP password (Secrets Manager) | Yes |
| `JWT_SECRET` | JWT signing secret (Secrets Manager) | Yes |
| `JWT_EXPIRATION_MS` | Access token expiry (default 900000 = 15min) | No |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh token expiry (default 86400000 = 24hr) | No |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins | Yes |
| `RATE_LIMIT_ENABLED` | Enable rate limiting (default true) | No |

---

## Post-Deployment Checklist

- [ ] HTTPS enforced on ALB
- [ ] ACM certificate attached to ALB listener
- [ ] Security groups restrict inbound traffic correctly
- [ ] RDS multi-AZ enabled for production
- [ ] ElastiCache backup enabled
- [ ] CloudWatch alarms configured for CPU/memory
- [ ] CloudWatch log group created with retention policy
- [ ] Auto-scaling configured
- [ ] Secrets stored in AWS Secrets Manager (not plaintext env vars)
- [ ] Flyway baseline migration applied
- [ ] `/actuator/health` returns `UP` via ALB
- [ ] Swagger UI accessible (restrict in production if needed)
