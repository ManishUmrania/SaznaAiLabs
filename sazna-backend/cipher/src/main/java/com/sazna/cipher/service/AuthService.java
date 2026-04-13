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

    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

    public AuthService( JwtTokenProvider jwtTokenProvider) {

        this.jwtTokenProvider = jwtTokenProvider;
        this.restTemplate = new RestTemplate();
    }

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            // Call identity service to get user details
            String identityServiceUrl = "http://localhost:8080/api/users/validate";

            // Make REST call to identity service
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(identityServiceUrl, loginRequest, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && (Boolean) response.getBody().get("valid")) {
                String token = jwtTokenProvider.generateToken(loginRequest.getEmail());

                return new LoginResponse(true, "Login successful", token);
            }
            return new LoginResponse(false, "Invalid credentials", null);
        } catch (RestClientException e) {
            return new LoginResponse(false, "Error connecting to identity service", null);
        }
    }
}