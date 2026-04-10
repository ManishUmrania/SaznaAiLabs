# Testing and Quality Assurance Guide

This guide provides comprehensive information about testing strategies, quality assurance practices, and automation for the Sazna Platform.

## Table of Contents

- [Testing Strategy](#testing-strategy)
- [Unit Testing](#unit-testing)
  - [AuthService Testing](#authservice-testing)
  - [UserService Testing](#userservice-testing)
- [Integration Testing](#integration-testing)
- [API Testing](#api-testing)
- [Security Testing](#security-testing)
- [Performance Testing](#performance-testing)
- [Test Automation](#test-automation)
- [Continuous Integration](#continuous-integration)
- [Quality Gates](#quality-gates)
- [Code Coverage](#code-coverage)

## Testing Strategy

The Sazna Platform follows a comprehensive testing pyramid approach:

1. **Unit Tests** (70%) - Test individual components in isolation
2. **Integration Tests** (20%) - Test service interactions and database operations
3. **API/Contract Tests** (7%) - Test API endpoints and contracts
4. **End-to-End Tests** (3%) - Test complete user workflows

## Unit Testing

### AuthService Testing

Test the authentication service components:

```java
@SpringBootTest
class AuthServiceTest {
    
    @MockBean
    private PasswordEncoderService passwordEncoderService;
    
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private AuthService authService;
    
    @Test
    void testSuccessfulLogin() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        
        // Mock external service response
        mockIdentityServiceResponse();
        
        // Mock password validation
        when(passwordEncoderService.matches(anyString(), anyString())).thenReturn(true);
        
        // Mock JWT generation
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("fake-jwt-token");
        
        // When
        LoginResponse response = authService.login(request);
        
        // Then
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getToken());
    }
    
    @Test
    void testFailedLoginWithInvalidCredentials() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");
        
        mockIdentityServiceResponse();
        when(passwordEncoderService.matches(anyString(), anyString())).thenReturn(false);
        
        // When
        LoginResponse response = authService.login(request);
        
        // Then
        assertFalse(response.isSuccess());
        assertEquals("Invalid credentials", response.getMessage());
        assertNull(response.getToken());
    }
}
```

### UserService Testing

Test user management functionality:

```java
@SpringBootTest
@Transactional
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testUserRegistration() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail("newuser@example.com");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPassword("password123");
        
        // When
        UserResponseDTO response = userService.registerLocalUser(request);
        
        // Then
        assertNotNull(response.getId());
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertTrue(response.isActive());
        
        // Verify user is saved in database
        Optional<User> savedUser = userRepository.findByEmail("newuser@example.com");
        assertTrue(savedUser.isPresent());
    }
    
    @Test
    void testUserProfileUpdate() {
        // Given
        User user = createTestUser();
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("Name");
        
        // When
        UserResponseDTO response = userService.updateProfile(user.getId(), updateDTO);
        
        // Then
        assertEquals("Updated", response.getFirstName());
        assertEquals("Name", response.getLastName());
    }
    
    @Test
    void testUserDeactivation() {
        // Given
        User user = createTestUser();
        
        // When
        userService.deactivateUser(user.getId());
        
        // Then
        User deactivatedUser = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(deactivatedUser);
        assertFalse(deactivatedUser.isActive());
    }
}
```

## Integration Testing

Test service integration with the database:

```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserIntegrationTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testUserPersistence() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail("integration@test.com");
        request.setFirstName("Integration");
        request.setLastName("Test");
        request.setPassword("password123");
        
        // When
        UserResponseDTO response = userService.registerLocalUser(request);
        
        // Then
        // Verify response
        assertEquals("integration@test.com", response.getEmail());
        
        // Verify database state
        Optional<User> userInDb = userRepository.findByEmail("integration@test.com");
        assertTrue(userInDb.isPresent());
        assertEquals("Integration", userInDb.get().getFirstName());
    }
}
```

## API Testing

Test REST API endpoints:

```java
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
class UserControllerTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService)).build();
    }
    
    @Test
    void testGetUserProfile() throws Exception {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("hashed-password");
        user.setActive(true);
        entityManager.persistAndFlush(user);
        
        // Mock authentication
        mockMvc.with(request -> {
            request.setAttribute("userId", user.getId());
            return request;
        });
        
        // When & Then
        mockMvc.perform(get("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }
    
    @Test
    void testUserRegistration() throws Exception {
        // Given
        String userJson = """
            {
                "email": "register@test.com",
                "firstName": "Register",
                "lastName": "Test",
                "password": "password123"
            }
            """;
        
        // When & Then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("register@test.com"))
                .andExpect(jsonPath("$.active").value(true));
    }
}
```

## Security Testing

Test authentication and authorization:

```java
@SpringBootTest
class SecurityTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testUnauthenticatedAccessDenied() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testInvalidJwtRejected() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testPublicEndpointsAccessible() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "test@example.com",
                        "password": "password123"
                    }
                    """))
                .andExpect(status().isOk());
    }
}
```

## Performance Testing

Load testing with JMeter or Gatling:

### JMeter Test Plan Example

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.4.1">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="Sazna Platform Load Test">
      <!-- Thread Group for Login API -->
      <hashTree>
        <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Login API Test">
          <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel">
            <intProp name="LoopController.loops">100</intProp>
          </elementProp>
          <stringProp name="ThreadGroup.num_threads">10</stringProp>
          <stringProp name="ThreadGroup.ramp_time">30</stringProp>
          <boolProp name="ThreadGroup.scheduler">false</boolProp>
          <stringProp name="ThreadGroup.duration"></stringProp>
          <stringProp name="ThreadGroup.delay"></stringProp>
        </ThreadGroup>
        <hashTree>
          <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="Login Request">
            <elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel">
              <collectionProp name="Arguments.arguments">
                <elementProp name="" elementType="HTTPArgument">
                  <stringProp name="Argument.value">{"email":"test@example.com","password":"password123"}</stringProp>
                  <stringProp name="Argument.metadata">=</stringProp>
                </elementProp>
              </collectionProp>
            </elementProp>
            <stringProp name="HTTPSampler.domain">localhost</stringProp>
            <stringProp name="HTTPSampler.port">8081</stringProp>
            <stringProp name="HTTPSampler.protocol">http</stringProp>
            <stringProp name="HTTPSampler.path">/api/auth/login</stringProp>
            <stringProp name="HTTPSampler.method">POST</stringProp>
          </HTTPSamplerProxy>
          <hashTree/>
        </hashTree>
      </hashTree>
    </TestPlan>
  </hashTree>
</jmeterTestPlan>
```

## Test Automation

### Gradle Test Configuration

```gradle
// In build.gradle
test {
    useJUnitPlatform()
    
    // System properties for tests
    systemProperty 'spring.profiles.active', 'test'
    
    // JVM arguments for tests
    jvmArgs '-Xmx1g', '-XX:+UseG1GC'
    
    // Test reporting
    reports {
        junitXml.required = true
        html.required = true
    }
    
    // Test filtering
    filter {
        includeTestsMatching "*Test"
        excludeTestsMatching "*IntegrationTest"
    }
}

// Separate task for integration tests
task integrationTest(type: Test) {
    useJUnitPlatform()
    systemProperty 'spring.profiles.active', 'integration'
    
    filter {
        includeTestsMatching "*IntegrationTest"
    }
    
    shouldRunAfter test
}

// Separate task for security tests
task securityTest(type: Test) {
    useJUnitPlatform()
    
    filter {
        includeTestsMatching "*SecurityTest"
    }
    
    shouldRunAfter integrationTest
}
```

### Test Scripts

```bash
#!/bin/bash
# test-runner.sh

echo "Running Sazna Platform Test Suite"

# Run unit tests
echo "Running unit tests..."
./gradlew test

if [ $? -ne 0 ]; then
    echo "Unit tests failed"
    exit 1
fi

# Run integration tests
echo "Running integration tests..."
./gradlew integrationTest

if [ $? -ne 0 ]; then
    echo "Integration tests failed"
    exit 1
fi

# Run security tests
echo "Running security tests..."
./gradlew securityTest

if [ $? -ne 0 ]; then
    echo "Security tests failed"
    exit 1
fi

echo "All tests passed successfully!"
```

## Continuous Integration

### GitHub Actions CI Pipeline

```yaml
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
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
    
    - name: Run unit tests
      run: ./gradlew test
      env:
        SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/sazna_test
        SPRING_DATASOURCE_USERNAME: postgres
        SPRING_DATASOURCE_PASSWORD: postgres
    
    - name: Run integration tests
      run: ./gradlew integrationTest
    
    - name: Run security tests
      run: ./gradlew securityTest
    
    - name: Publish Test Report
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: test-results
        path: |
          **/build/reports/tests/
          **/build/test-results/
```

## Quality Gates

### SonarQube Integration

```gradle
plugins {
    id "org.sonarqube" version "4.0.0.2929"
}

sonarqube {
    properties {
        property "sonar.projectKey", "sazna-platform"
        property "sonar.projectName", "Sazna Platform"
        property "sonar.projectVersion", "1.0"
        property "sonar.sources", "src/main"
        property "sonar.tests", "src/test"
        property "sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml"
    }
}
```

### Code Quality Checks

```gradle
plugins {
    id 'jacoco'
    id 'checkstyle'
    id 'pmd'
}

checkstyle {
    toolVersion = '10.0'
    configFile = file("config/checkstyle/checkstyle.xml")
}

pmd {
    toolVersion = '6.50.0'
    ruleSets = []
    ruleSetFiles = files("config/pmd/ruleset.xml")
}

jacoco {
    toolVersion = "0.8.8"
}

test {
    finalizedBy jacocoTestReport
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true
        html.required = true
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = 'CLASS'
            limit {
                counter = 'LINE'
                value = 'COVEREDRATIO'
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
```

## Code Coverage

### Jacoco Configuration

```gradle
// In build.gradle
jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true
        html.required = true
        csv.required = false
        
        html.outputLocation = layout.buildDirectory.dir('reports/jacoco/html')
        xml.outputLocation = layout.buildDirectory.file('reports/jacoco/test/jacocoTestReport.xml')
    }
    
    // Exclude generated code and configuration classes
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/*Application*',
                '**/*Config*',
                '**/*DTO*',
                '**/*Entity*'
            ])
        }))
    }
}
```

### Coverage Requirements

Minimum coverage thresholds:
- Overall: 80%
- Critical Services (AuthService, UserService): 90%
- Controllers: 85%
- Repositories: 95%

This testing and QA guide provides a comprehensive approach to ensuring the quality, reliability, and security of the Sazna Platform through various testing methodologies and automation practices.