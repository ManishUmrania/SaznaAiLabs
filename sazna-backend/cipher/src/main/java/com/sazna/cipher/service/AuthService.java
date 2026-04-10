package com.sazna.cipher.service;

import com.sazna.cipher.security.JwtTokenProvider;
import com.sazna.cipher.security.PasswordEncoderService;
import com.sazna.shared.dto.LoginRequest;
import com.sazna.shared.dto.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthService {

    private final PasswordEncoderService passwordEncoderService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

    public AuthService(PasswordEncoderService passwordEncoderService, JwtTokenProvider jwtTokenProvider) {
        this.passwordEncoderService = passwordEncoderService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.restTemplate = new RestTemplate();
    }

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            // Call identity service to get user details
            String identityServiceUrl = "http://localhost:8080/api/users/email/" + loginRequest.getEmail();

            // Make REST call to identity service
            ResponseEntity<Map> response = restTemplate.getForEntity(identityServiceUrl, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> userMap = response.getBody();

                // Extract password from response (in a real implementation, this would be properly mapped)
                // For now, we'll use a simulated hashed password
                String hashedPasswordFromDB = "$2a$10$njXRKQkMGBcwgHGUrD/lYOvFqGCkyhV5EyQR5LwGacvRcWpfsP0oG"; // "password" hashed

                boolean passwordMatches = passwordEncoderService.matches(loginRequest.getPassword(), hashedPasswordFromDB);

                if (passwordMatches) {
                    // Generate JWT token
                    String token = jwtTokenProvider.generateToken(loginRequest.getEmail());

                    LoginResponse loginResponse = new LoginResponse();
                    loginResponse.setSuccess(true);
                    loginResponse.setMessage("Login successful");
                    loginResponse.setToken(token);
                    return loginResponse;
                } else {
                    LoginResponse loginResponse = new LoginResponse();
                    loginResponse.setSuccess(false);
                    loginResponse.setMessage("Invalid credentials");
                    return loginResponse;
                }
            } else {
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setSuccess(false);
                loginResponse.setMessage("User not found");
                return loginResponse;
            }
        } catch (RestClientException e) {
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setSuccess(false);
            loginResponse.setMessage("Service unavailable: " + e.getMessage());
            return loginResponse;
        } catch (Exception e) {
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setSuccess(false);
            loginResponse.setMessage("Login failed: " + e.getMessage());
            return loginResponse;
        }
    }
}