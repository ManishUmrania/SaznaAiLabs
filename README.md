# Sazna Platform

Sazna Platform is a modular microservices-based authentication and identity management system built with Spring Boot. The platform consists of two core services that work together to provide secure user authentication and identity management.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Services](#services)
  - [Cipher Service (Authentication)](#cipher-service-authentication)
  - [Identity Service (User Management)](#identity-service-user-management)[.gitignore](.gitignore)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [API Endpoints](#api-endpoints)
- [Security](#security)
- [Development](#development)

## Architecture Overview

The Sazna Platform follows a modular monolith architecture with clearly separated concerns:

```
┌─────────────────┐    ┌──────────────────┐
│   Cipher        │    │    Identity      │
│  (Auth)         │    │   (Users)        │
│                 │    │                  │
│ JWT Generation  │◄──►│ User Management  │
│ Authentication  │    │ Profile CRUD     │
│ Password Hashing│    │ Prov[.gitignore](.gitignore)ider Sync    │
└─────────────────┘    └──────────────────┘
          │                      │
          └──────────────────────┘
                   │
            ┌─────────────┐
            │   Shared    │
            │   Models    │
            └─────────────┘
```

## Services

### Cipher Service (Authentication)

The Cipher service handles authentication operations including JWT token generation, validation, and password management.

**Key Responsibilities:**
- User authentication via email/password
- JWT token creation and validation
- Password encoding and verification
- Authentication middleware/filter

### Identity Service (User Management)

The Identity service manages user profiles, registration, and synchronization with external providers.

**Key Responsibilities:**
- User profile management (CRUD operations)
- User registration and account management
- Synchronization with external authentication providers
- User data persistence

## Features

### Implemented Features

1. **JWT-based Authentication**
   - Secure token generation with configurable expiration
   - Token validation middleware
   - Stateless authentication

2. **User Management**
   - User registration with email/password
   - Profile retrieval and updates
   - Account deactivation
   - External provider synchronization

3. **Security**
   - Password hashing with BCrypt
   - Role-based access control
   - CSRF protection disabled for API-first approach
   - Stateless session management

4. **External Provider Integration**
   - OAuth provider synchronization
   - Automatic user creation for new provider users

### Planned Features

1. **OAuth Integration**
   - Google/GitHub authentication support
   - Social login flows

2. **Enhanced Security**
   - Two-factor authentication
   - Password reset workflows
   - Account lockout mechanisms

3. **Admin Features**
   - User management dashboard
   - Role and permission management
   - Audit logging

## Technology Stack

- **Java 25** - Primary programming language
- **Spring Boot 4.0.5** - Framework for building microservices
- **Spring Security** - Authentication and authorization
- **JWT (JSON Web Tokens)** - Token-based authentication
- **PostgreSQL** - Primary database
- **H2 Database** - Development/testing database
- **Gradle** - Build automation tool
- **Lombok** - Boilerplate code reduction

## Getting Started

### Prerequisites

- Java 25+
- PostgreSQL database
- Gradle 9.2+

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd sazna-platform
   ```

2. Configure database settings in `sazna-backend/identity/src/main/resources/application.yml`

3. Build the project:
   ```bash
   ./gradlew build
   ```

4. Run the services:
   ```bash
   ./gradlew :cipher:bootRun
   ./gradlew :identity:bootRun
   ```

## API Endpoints

### Authentication Service (Port 8081)

#### POST `/api/auth/login`
Authenticate a user with email and password
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

#### POST `/api/auth/logout`
Logout the current user

### Identity Service (Port 8080)

#### POST `/api/users/signup`
Register a new user
```json
{
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "password123"
}
```

#### GET `/api/users/me`
Retrieve current user profile (requires authentication)

#### PATCH `/api/users/me`
Update current user profile (requires authentication)

#### DELETE `/api/users/me`
Deactivate current user account (requires authentication)

#### POST `/api/users/sync`
Synchronize user with external provider (used internally)

#### GET `/api/users/email/{email}`
Retrieve user by email (internal service communication)

## Security

The platform implements several security measures:

- **JWT Tokens**: Secure, stateless authentication mechanism
- **Password Hashing**: BCrypt algorithm for secure password storage
- **Role-Based Access**: Different access levels for various operations
- **Input Validation**: Request validation to prevent injection attacks
- **CSRF Protection**: Disabled for API-first approach but can be enabled if needed

JWT tokens are configured with a 24-hour expiration by default and use HMAC SHA-512 signing.

## Development

### Project Structure

```
sazna-platform/
├── sazna-backend/
│   ├── cipher/           # Authentication service
│   │   ├── src/main/java/com/sazna/cipher/
│   │   │   ├── config/   # Security configuration
│   │   │   ├── controller/ # Authentication endpoints
│   │   │   ├── security/ # JWT and security components
│   │   │   └── service/  # Authentication business logic
│   │   └── src/main/resources/
│   │       └── application.yml # Service configuration
│   ├── identity/         # User management service
│   │   ├── src/main/java/com/sazna/identity/
│   │   │   ├── controller/ # User endpoints
│   │   │   ├── dto/      # Data transfer objects
│   │   │   ├── entity/   # JPA entities
│   │   │   ├── repository/ # Data access layer
│   │   │   └── service/  # User business logic
│   │   └── src/main/resources/
│   │       └── application.yml # Service configuration
│   └── Shared/           # Common models and DTOs
│       └── src/main/java/com/sazna/shared/
│           └── dto/      # Shared data transfer objects
├── build.gradle          # Root build configuration
└── settings.gradle       # Project module configuration
```

### Module Dependencies

- **cipher**: Depends on `shared` module for DTOs
- **identity**: Depends on `shared` module for DTOs
- **shared**: No dependencies, contains shared models

### Building and Testing

Build the entire project:
```bash
./gradlew build
```

Run tests:
```bash
./gradlew test
```

Run individual services:
```bash
./gradlew :cipher:bootRun
./gradlew :identity:bootRun
```

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.