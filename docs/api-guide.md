# API Guide

This document provides comprehensive information about the Sazna Platform APIs, including authentication flows, user management endpoints, and integration examples.

## Table of Contents

- [Authentication Flow](#authentication-flow)
- [Cipher Service API](#cipher-service-api)
  - [Login](#login)
  - [Logout](#logout)
- [Identity Service API](#identity-service-api)
  - [User Registration](#user-registration)
  - [Profile Management](#profile-management)
  - [Account Management](#account-management)
  - [External Provider Sync](#external-provider-sync)
- [Error Handling](#error-handling)
- [API Examples](#api-examples)
- [Security Considerations](#security-considerations)

## Authentication Flow

The Sazna Platform uses JWT (JSON Web Token) based authentication:

1. **User Authentication**: Client sends credentials to Cipher service
2. **Token Generation**: Cipher service validates credentials and generates JWT
3. **Token Usage**: Client includes JWT in Authorization header for subsequent requests
4. **Token Validation**: Services validate JWT before processing requests

```sequence
Client->Cipher Service: POST /api/auth/login {email, password}
Cipher Service->Identity Service: GET /api/users/email/{email}
Identity Service-->Cipher Service: User data
Cipher Service->Cipher Service: Validate password
Cipher Service-->Client: JWT Token
Client->Identity Service: GET /api/users/me {Authorization: Bearer JWT}
Identity Service->Identity Service: Validate JWT
Identity Service-->Client: User profile
```

## Cipher Service API

Base URL: `http://localhost:8081`

### Login

Authenticate a user and receive a JWT token.

**Endpoint**: `POST /api/auth/login`

**Request**:
```json
{
  "email": "user@example.com",
  "password": "userpassword"
}
```

**Response** (Success):
```json
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzUxMiJ9.xxxx"
}
```

**Response** (Failure):
```json
{
  "success": false,
  "message": "Invalid credentials"
}
```

### Logout

Log out the current user (invalidate the JWT token).

**Endpoint**: `POST /api/auth/logout`

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Response**:
```json
{
  "message": "Logged out successfully"
}
```

## Identity Service API

Base URL: `http://localhost:8080`

### User Registration

Register a new user with email and password.

**Endpoint**: `POST /api/users/signup`

**Request**:
```json
{
  "email": "newuser@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "securepassword123"
}
```

**Response**:
```json
{
  "id": 1,
  "email": "newuser@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "active": true
}
```

### Profile Management

#### Get Current User Profile

Retrieve the profile of the authenticated user.

**Endpoint**: `GET /api/users/me`

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Response**:
```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "active": true
}
```

#### Update Current User Profile

Update the profile of the authenticated user.

**Endpoint**: `PATCH /api/users/me`

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request**:
```json
{
  "firstName": "Jane",
  "lastName": "Smith"
}
```

**Response**:
```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "active": true
}
```

### Account Management

#### Deactivate Account

Deactivate the current user's account (soft delete).

**Endpoint**: `DELETE /api/users/me`

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Response**:
```
HTTP 204 No Content
```

#### Get User by Email

Retrieve user information by email (internal service communication).

**Endpoint**: `GET /api/users/email/{email}`

**Response**:
```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "active": true
}
```

### External Provider Sync

Synchronize user data with external authentication providers.

**Endpoint**: `POST /api/users/sync`

**Request**:
```json
{
  "email": "oauthuser@example.com",
  "firstName": "OAuth",
  "lastName": "User"
}
```

**Response**:
```json
{
  "id": 2,
  "email": "oauthuser@example.com",
  "firstName": "OAuth",
  "lastName": "User",
  "active": true
}
```

## Error Handling

The API follows standard HTTP status codes:

| Status Code | Description |
|-------------|-------------|
| 200 | Successful request |
| 201 | Resource created |
| 204 | No content (successful delete) |
| 400 | Bad request (validation errors) |
| 401 | Unauthorized (invalid credentials) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Not found |
| 500 | Internal server error |

Error responses follow this format:
```json
{
  "timestamp": "2026-04-10T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for argument",
  "path": "/api/users/signup"
}
```

## API Examples

### Complete Authentication Flow

1. **Register a new user**:
```bash
curl -X POST http://localhost:8080/api/users/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User",
    "password": "password123"
  }'
```

2. **Login to get JWT token**:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

3. **Use token to access protected endpoints**:
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.xxxx"
```

### JavaScript/Fetch Example

```javascript
// Login and store token
async function login(email, password) {
  const response = await fetch('http://localhost:8081/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ email, password })
  });
  
  const data = await response.json();
  
  if (data.success) {
    localStorage.setItem('authToken', data.token);
    return data.token;
  } else {
    throw new Error(data.message);
  }
}

// Fetch user profile
async function getProfile(token) {
  const response = await fetch('http://localhost:8080/api/users/me', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return response.json();
}
```

### Python/Requests Example

```python
import requests

# Base URLs
CIPHER_URL = "http://localhost:8081"
IDENTITY_URL = "http://localhost:8080"

def login(email, password):
    """Authenticate user and return JWT token"""
    response = requests.post(
        f"{CIPHER_URL}/api/auth/login",
        json={"email": email, "password": password}
    )
    
    data = response.json()
    if data["success"]:
        return data["token"]
    else:
        raise Exception(data["message"])

def get_profile(token):
    """Get user profile using JWT token"""
    response = requests.get(
        f"{IDENTITY_URL}/api/users/me",
        headers={"Authorization": f"Bearer {token}"}
    )
    
    return response.json()

# Usage
try:
    token = login("test@example.com", "password123")
    profile = get_profile(token)
    print(f"Welcome, {profile['firstName']} {profile['lastName']}!")
except Exception as e:
    print(f"Error: {e}")
```

## Security Considerations

### JWT Security

1. **Token Storage**: Store JWTs securely (HttpOnly cookies or secure local storage)
2. **Token Expiration**: Use short-lived tokens (configured to 24 hours by default)
3. **HTTPS**: Always use HTTPS in production to prevent token interception
4. **Token Revocation**: Implement logout by maintaining a blacklist of revoked tokens

### Password Security

1. **Hashing**: Passwords are hashed using BCrypt with configurable cost factors
2. **Transmission**: Passwords should only be transmitted over HTTPS
3. **Validation**: Implement password strength requirements in production

### CORS and CSRF

1. **CORS Configuration**: Properly configure CORS for trusted origins only
2. **CSRF Protection**: Currently disabled for API-first approach but can be enabled if needed

### Rate Limiting

Consider implementing rate limiting for authentication endpoints to prevent brute-force attacks:

```yaml
# Example configuration
spring:
  cloud:
    gateway:
      routes:
        - id: auth-rate-limit
          uri: lb://cipher-service
          predicates:
            - Path=/api/auth/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter:
                  replenishRate: 10
                  burstCapacity: 20
```

### Input Validation

All endpoints implement validation:

1. **Email Format**: Valid email format required
2. **Password Strength**: Minimum length requirements
3. **Field Constraints**: Size limits and character restrictions
4. **SQL Injection**: Parameterized queries prevent injection attacks

This API guide provides everything needed to integrate with the Sazna Platform services. The JWT-based authentication ensures secure, stateless communication between clients and services.