# Local Development Setup Guide

This guide provides comprehensive instructions for setting up a local development environment for the Sazna Platform.

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Development Approaches](#development-approaches)
  - [Monolithic Style (Single Container)](#monolithic-style-single-container)
  - [Microservices Style (Separate Containers)](#microservices-style-separate-containers)
- [Automated Scripts](#automated-scripts)
- [Configuration Management](#configuration-management)
- [Debugging](#debugging)
- [CI/CD Integration](#cicd-integration)

## Overview

The Sazna Platform can be developed locally using different approaches depending on your needs:

1. **Containerized Development** - Using Docker for consistent environments
2. **Direct JVM Execution** - Running services directly on your machine
3. **Hybrid Approach** - Mixing containers for dependencies with direct service execution

## Prerequisites

Before setting up your local environment, ensure you have the following installed:

- **Java 25+** - JDK for compiling and running services
- **Gradle 9.2+** - Build tool for the project
- **Docker Desktop** - Containerization platform (recommended)
- **Docker Compose** - Orchestration tool for multi-container applications
- **PostgreSQL Client** - For database interactions (psql, pgAdmin, etc.)
- **IDE with Java Support** - IntelliJ IDEA, VS Code with Java extensions, or Eclipse

## Quick Start

For the fastest setup, use our automated scripts:

### Unix/Linux/macOS:
```bash
# Make scripts executable
chmod +x scripts/*.sh

# Build the project
./scripts/build.sh

# Run in development mode
./scripts/dev-run.sh
```

### Windows:
```cmd
# Build the project
scripts\build.bat

# Run in development mode
scripts\dev-run.bat
```

## Development Approaches

### Monolithic Style (Single Container)

For simplified local development, you can run all services in a single container initially:

#### Benefits:
- Simplified networking
- Easier debugging
- Faster startup times
- Reduced resource consumption

#### Implementation:
Create a combined Dockerfile that includes both services:

```dockerfile
# Combined services Dockerfile (for initial development)
FROM openjdk:25-jdk-slim

WORKDIR /app

# Copy both services
COPY sazna-backend/cipher/build/libs/cipher-0.0.1-SNAPSHOT.jar cipher.jar
COPY sazna-backend/identity/build/libs/identity-0.0.1-SNAPSHOT.jar identity.jar

# Expose both ports
EXPOSE 8080 8081

# Start both services (you would need a process manager like supervisord)
CMD ["sh", "-c", "java -jar identity.jar & java -jar cipher.jar"]
```

### Microservices Style (Separate Containers)

For production-like development and testing:

#### Benefits:
- Production parity
- Independent scaling
- Service isolation
- Better resource management

#### Implementation:
Use the provided `docker-compose.yml`:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    # ... database configuration
  
  identity-service:
    build: ./sazna-backend/identity
    ports:
      - "8080:8080"
    # ... service configuration
  
  cipher-service:
    build: ./sazna-backend/cipher
    ports:
      - "8081:8081"
    # ... service configuration
```

Run with:
```bash
docker-compose up --build
```

## Automated Scripts

We provide several scripts to automate common development tasks:

### Build Automation (`scripts/build.sh` | `scripts/build.bat`)

- Compiles all services
- Runs Gradle build excluding tests for faster iteration
- Prepares JAR files for Docker builds

### Development Runner (`scripts/dev-run.sh` | `scripts/dev-run.bat`)

- Starts PostgreSQL database in Docker
- Runs both services directly on the JVM
- Provides easy start/stop functionality

### Docker Compose Operations

```bash
# Build all services
docker-compose build

# Start all services
docker-compose up

# Start specific service
docker-compose up identity-service

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

## Configuration Management

### Environment Variables

Centralize configuration using environment variables:

```bash
# Database configuration
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sazna_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres

# Service URLs
export IDENTITY_SERVICE_URL=http://localhost:8080
export CIPHER_SERVICE_URL=http://localhost:8081
```

### Configuration Files

Each service has its own `application.yml` for environment-specific settings:

**Identity Service** (`sazna-backend/identity/src/main/resources/application.yml`):
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sazna_db
    username: postgres
    password: postgres
```

**Cipher Service** (`sazna-backend/cipher/src/main/resources/application.yml`):
```yaml
server:
  port: 8081

jwt:
  secret: mySecretKey
  expiration: 86400000
```

### Profile-based Configuration

Use Spring Profiles for different environments:

```bash
# Development profile
./gradlew :identity:bootRun --args='--spring.profiles.active=dev'

# Production profile
./gradlew :identity:bootRun --args='--spring.profiles.active=prod'
```

## Debugging

### JVM Debugging

Enable remote debugging for services:

```bash
# Identity Service with debug enabled
./gradlew :identity:bootRun --debug-jvm

# Custom debug configuration
JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" \
./gradlew :identity:bootRun
```

### Docker Debugging

Debug services running in containers:

```bash
# Run with debug ports exposed
docker run -p 8080:8080 -p 5005:5005 --name identity-debug \
  -e JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" \
  sazna-identity
```

### Logging

Configure detailed logging for troubleshooting:

```yaml
logging:
  level:
    com.sazna: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

### Health Checks

Monitor service health:

```bash
# Identity Service health
curl http://localhost:8080/actuator/health

# Cipher Service health
curl http://localhost:8081/actuator/health
```

## CI/CD Integration

### GitHub Actions Workflow

Create `.github/workflows/ci.yml`:

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: sazna_test
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 25
      uses: actions/setup-java@v3
      with:
        java-version: '25'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew build
    
    - name: Run tests
      run: ./gradlew test
```

### Docker Image Publishing

Automate Docker image building and publishing:

```yaml
- name: Build and push Docker images
  run: |
    docker build -t sazna/identity:$GITHUB_SHA ./sazna-backend/identity
    docker build -t sazna/cipher:$GITHUB_SHA ./sazna-backend/cipher
    docker push sazna/identity:$GITHUB_SHA
    docker push sazna/cipher:$GITHUB_SHA
```

### Deployment Strategies

#### Blue-Green Deployment

```bash
# Deploy new version
docker-compose -f docker-compose-new.yml up -d

# Health check new version
curl -f http://localhost:8080/actuator/health

# Switch traffic
docker-compose -f docker-compose-old.yml down
```

#### Rolling Updates

```bash
# Update one service at a time
docker-compose up -d --no-deps --scale identity-service=2 identity-service
docker-compose up -d --no-deps identity-service
```

## Best Practices

### Local Development

1. **Use consistent environments** - Docker ensures everyone runs the same setup
2. **Enable hot reloading** - Use Spring Boot DevTools for faster iteration
3. **Separate configs** - Use different profiles for dev/test/prod
4. **Health checks** - Implement proper health endpoints
5. **Logging** - Use structured logging for better debugging

### Containerization Guidelines

1. **Multi-stage builds** - Reduce image size
2. **Non-root user** - Security best practice
3. **Health checks** - Monitor service status
4. **Resource limits** - Prevent resource exhaustion
5. **Graceful shutdown** - Handle SIGTERM properly

### Scripting Best Practices

1. **Cross-platform compatibility** - Provide both shell and batch scripts
2. **Error handling** - Check exit codes and handle failures
3. **Documentation** - Comment scripts thoroughly
4. **Idempotency** - Scripts should be safe to run multiple times
5. **Configuration** - Make scripts configurable via environment variables

This setup provides a robust foundation for local development that scales seamlessly into production deployments and CI/CD pipelines.