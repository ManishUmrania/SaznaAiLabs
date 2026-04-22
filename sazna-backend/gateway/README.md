# API Gateway Service

## Overview
This is the API Gateway service for the Sazna Platform. It acts as a single entry point for all client requests and routes them to the appropriate microservices.

## Architecture
- **Spring Cloud Gateway**: Used for routing, filtering, and load balancing
- **WebFlux**: Reactive programming model for high performance
- **JWT Authentication**: Centralized authentication handling
- **Eureka Client**: Service discovery integration
- **Redis**: Rate limiting implementation

## Features
1. **Dynamic Routing**: Routes requests to appropriate microservices using service discovery
2. **Authentication**: Validates JWT tokens for protected endpoints
3. **CORS**: Cross-origin resource sharing configuration
4. **Rate Limiting**: Controls request frequency to prevent abuse
5. **Load Balancing**: Distributes requests across service instances
6. **Health Checks**: Monitoring endpoints for service status
7. **Error Handling**: Consistent error responses across all routes

## Routes
- `/api/users/**` → Identity Service (discovered via Eureka)
- `/api/auth/**` → Cipher Service (discovered via Eureka)

## Security
- Public endpoints: `/api/users/signup`, `/api/users/login`, `/api/auth/login`
- All other endpoints require valid JWT token
- JWT validation using public key cryptography
- IP-based rate limiting to prevent abuse

## Configuration
The gateway is configured via `application.yml`:
- Port: 8085
- CORS settings for frontend integration
- Service discovery with Eureka
- JWT public key path
- Rate limiting with Redis
- Health check endpoints

## Filters
- **JwtAuthenticationGatewayFilter**: Custom filter for JWT token validation and user context propagation
- **RequestRateLimiter**: Rate limiting filter to control request frequency

## Best Practices Implemented
1. **Separation of Concerns**: Gateway handles routing and authentication only
2. **Reactive Programming**: Non-blocking I/O for better performance
3. **Centralized Security**: Single point of authentication validation
4. **Service Isolation**: Microservices don't communicate directly with each other
5. **Extensibility**: Easy to add new routes and filters
6. **Resilience**: Health checks and error handling for robust operation
7. **Observability**: Comprehensive logging and monitoring support