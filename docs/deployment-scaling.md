# Deployment and Scaling Guide

This guide provides comprehensive instructions for deploying and scaling the Sazna Platform in various environments, from local development to production.

## Table of Contents

- [Deployment Options](#deployment-options)
- [Container Orchestration](#container-orchestration)
  - [Docker Swarm](#docker-swarm)
  - [Kubernetes](#kubernetes)
- [Production Configuration](#production-configuration)
- [Scaling Strategies](#scaling-strategies)
  - [Horizontal Scaling](#horizontal-scaling)
  - [Vertical Scaling](#vertical-scaling)
  - [Auto-scaling](#auto-scaling)
- [Load Balancing](#load-balancing)
- [Monitoring and Observability](#monitoring-and-observability)
- [Backup and Disaster Recovery](#backup-and-disaster-recovery)
- [Security Hardening](#security-hardening)
- [Performance Optimization](#performance-optimization)

## Deployment Options

### Local Development Deployment

For local development, use Docker Compose:

```bash
# Build and start all services
docker-compose up --build

# Scale specific services
docker-compose up --scale identity-service=3

# View logs
docker-compose logs -f
```

### Staging Environment Deployment

For staging environments, use enhanced Docker Compose with resource limits:

```yaml
version: '3.8'
services:
  identity-service:
    # ... other config
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
        reservations:
          cpus: '0.25'
          memory: 256M
```

### Production Deployment

For production, consider Kubernetes or cloud provider managed services.

## Container Orchestration

### Docker Swarm

Deploy to Docker Swarm for simple orchestration:

```bash
# Initialize swarm
docker swarm init

# Deploy stack
docker stack deploy -c docker-compose.prod.yml sazna-platform

# Scale services
docker service scale sazna-platform_identity-service=3
```

Production-ready Docker Compose for Swarm:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: sazna_db
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    deploy:
      replicas: 1
      placement:
        constraints:
          - node.role == worker
      resources:
        limits:
          cpus: '0.5'
          memory: 512M

  identity-service:
    image: sazna/identity-service:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/sazna_db
      SPRING_PROFILES_ACTIVE: prod
    deploy:
      replicas: 3
      update_config:
        parallelism: 1
        delay: 10s
      rollback_config:
        parallelism: 1
        delay: 10s
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
      resources:
        limits:
          cpus: '0.5'
          memory: 512M

  cipher-service:
    image: sazna/cipher-service:latest
    ports:
      - "8081:8081"
    environment:
      IDENTITY_SERVICE_URL: http://identity-service:8080
      JWT_SECRET: ${JWT_SECRET}
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '0.5'
          memory: 256M

volumes:
  postgres_data:
```

### Kubernetes

For production-scale deployments, use Kubernetes:

#### Namespace and Secrets

```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: sazna-platform
```

```yaml
# secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: sazna-secrets
  namespace: sazna-platform
type: Opaque
data:
  db-password: <base64-encoded-password>
  jwt-secret: <base64-encoded-secret>
```

#### PostgreSQL Deployment

```yaml
# postgres-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: sazna-platform
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:15-alpine
        env:
        - name: POSTGRES_DB
          value: sazna_db
        - name: POSTGRES_USER
          value: postgres
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: sazna-secrets
              key: db-password
        ports:
        - containerPort: 5432
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
      volumes:
      - name: postgres-storage
        persistentVolumeClaim:
          claimName: postgres-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: postgres
  namespace: sazna-platform
spec:
  selector:
    app: postgres
  ports:
  - protocol: TCP
    port: 5432
    targetPort: 5432
```

#### Identity Service Deployment

```yaml
# identity-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: identity-service
  namespace: sazna-platform
spec:
  replicas: 3
  selector:
    matchLabels:
      app: identity-service
  template:
    metadata:
      labels:
        app: identity-service
    spec:
      containers:
      - name: identity-service
        image: sazna/identity-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:postgresql://postgres:5432/sazna_db
        - name: SPRING_DATASOURCE_USERNAME
          value: postgres
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: sazna-secrets
              key: db-password
        - name: SPRING_PROFILES_ACTIVE
          value: prod
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
---
apiVersion: v1
kind: Service
metadata:
  name: identity-service
  namespace: sazna-platform
spec:
  selector:
    app: identity-service
  ports:
  - protocol: TCP
    port: 8080
    targetPort: 8080
  type: ClusterIP
```

#### Cipher Service Deployment

```yaml
# cipher-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cipher-service
  namespace: sazna-platform
spec:
  replicas: 3
  selector:
    matchLabels:
      app: cipher-service
  template:
    metadata:
      labels:
        app: cipher-service
    spec:
      containers:
      - name: cipher-service
        image: sazna/cipher-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: IDENTITY_SERVICE_URL
          value: http://identity-service:8080
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: sazna-secrets
              key: jwt-secret
        - name: SPRING_PROFILES_ACTIVE
          value: prod
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "250m"
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: cipher-service
  namespace: sazna-platform
spec:
  selector:
    app: cipher-service
  ports:
  - protocol: TCP
    port: 8081
    targetPort: 8081
  type: ClusterIP
```

#### Ingress Controller

```yaml
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: sazna-ingress
  namespace: sazna-platform
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: api.sazna.example.com
    http:
      paths:
      - path: /identity
        pathType: Prefix
        backend:
          service:
            name: identity-service
            port:
              number: 8080
      - path: /auth
        pathType: Prefix
        backend:
          service:
            name: cipher-service
            port:
              number: 8081
```

## Production Configuration

### Environment-Specific Settings

Create separate configuration files for different environments:

**application-prod.yml**:
```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://postgres:5432/sazna_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized

logging:
  level:
    com.sazna: INFO
    org.springframework.web: WARN
    org.springframework.security: WARN
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/sazna-platform.log
```

### JVM Optimization

Production JVM settings:

```bash
# In Dockerfile or startup script
JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

## Scaling Strategies

### Horizontal Scaling

Scale services independently based on demand:

```bash
# Docker Swarm
docker service scale sazna-platform_cipher-service=5

# Kubernetes
kubectl scale deployment identity-service --replicas=5 -n sazna-platform
```

### Vertical Scaling

Increase resources for individual services:

```yaml
# Docker Swarm
deploy:
  resources:
    limits:
      cpus: '1.0'
      memory: 1G
    reservations:
      cpus: '0.5'
      memory: 512M

# Kubernetes
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

### Auto-scaling

#### Kubernetes Horizontal Pod Autoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: identity-service-hpa
  namespace: sazna-platform
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: identity-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

## Load Balancing

### Internal Load Balancing

Kubernetes Services provide internal load balancing:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: identity-service
spec:
  selector:
    app: identity-service
  ports:
  - protocol: TCP
    port: 8080
    targetPort: 8080
  type: ClusterIP  # Internal load balancing
```

### External Load Balancing

Use Ingress controllers or cloud load balancers:

```yaml
# Nginx Ingress with load balancing
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: sazna-ingress
  annotations:
    kubernetes.io/ingress.class: nginx
    nginx.ingress.kubernetes.io/upstream-hash-by: $remote_addr
spec:
  rules:
  - host: api.sazna.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: cipher-service
            port:
              number: 8081
```

## Monitoring and Observability

### Health Checks

Implement comprehensive health checks:

```java
@RestController
public class HealthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/actuator/health/readiness")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> health = new HashMap<>();
        
        // Check database connectivity
        try {
            userRepository.count();
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
        
        health.put("status", "UP");
        return ResponseEntity.ok(health);
    }
}
```

### Metrics Collection

Expose Prometheus metrics:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        all: true
```

### Logging Strategy

Centralized logging with structured formats:

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} [%X{traceId:-},%X{spanId:-}] - %msg%n"
  file:
    name: logs/sazna-platform.log
```

## Backup and Disaster Recovery

### Database Backups

Regular database backup strategy:

```bash
# PostgreSQL backup script
#!/bin/bash
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
pg_dump -h postgres -U postgres sazna_db > backups/sazna_backup_$TIMESTAMP.sql

# Retain only last 30 days of backups
find backups/ -name "sazna_backup_*.sql" -mtime +30 -delete
```

### Kubernetes Backup

Backup Kubernetes resources:

```bash
# Backup all resources in namespace
kubectl get all -n sazna-platform -o yaml > sazna-backup-$(date +%Y%m%d).yaml

# Backup secrets separately (encrypted)
kubectl get secrets -n sazna-platform -o yaml > sazna-secrets-backup-$(date +%Y%m%d).yaml
```

## Security Hardening

### Network Policies

Restrict network traffic between services:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: identity-service-policy
  namespace: sazna-platform
spec:
  podSelector:
    matchLabels:
      app: identity-service
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: cipher-service
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: postgres
    ports:
    - protocol: TCP
      port: 5432
```

### TLS Configuration

Enforce HTTPS communication:

```yaml
# Ingress with TLS
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: sazna-ingress
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - api.sazna.example.com
    secretName: sazna-tls
  rules:
  - host: api.sazna.example.com
    http:
      paths:
      # ... paths
```

## Performance Optimization

### Database Connection Pooling

Optimize database connections:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

### Caching Strategy

Implement caching for frequently accessed data:

```java
@Service
public class UserService {
    
    @Cacheable(value = "users", key = "#id")
    public UserResponseDTO getProfile(Long id) {
        // ... implementation
    }
    
    @CacheEvict(value = "users", key = "#id")
    public UserResponseDTO updateProfile(Long id, UserUpdateDTO dto) {
        // ... implementation
    }
}
```

### JVM Tuning

Production JVM optimizations:

```bash
JAVA_OPTS="
-server
-Xmx2g
-Xms1g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UnlockExperimentalVMOptions
-XX:+UseStringDeduplication
-XX:+OptimizeStringConcat
-Djava.security.egd=file:/dev/./urandom
"
```

This deployment and scaling guide provides a comprehensive approach to running the Sazna Platform in production environments, with considerations for high availability, performance, and security.