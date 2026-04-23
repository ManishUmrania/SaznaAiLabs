package com.sazna.identity.controller;

import com.sazna.identity.entity.User;
import com.sazna.identity.service.UserService;
import com.sazna.shared.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // This is what your Cipher (Auth) service will call after a successful Google/GitHub login
    @PostMapping("/sync")
    public ResponseEntity<UserResponseDTO> syncUser(@RequestBody ProviderSyncRequest request) {
        return ResponseEntity.ok(userService.syncWithProvider(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponseDTO> updateMyProfile(
            Authentication authentication,
            @RequestBody UserUpdateDTO dto
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateProfile(userId, dto));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        userService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(userService.registerLocalUser(request));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.findByEmail(email);
            return ResponseEntity.ok(mapToDTO(user));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateUserResponse> validateUser(
            @RequestBody ValidateUserRequest request) {

        ValidateUserResponse response = userService.validateUser(request);
        return ResponseEntity.ok(response);
    }



    // Helper method to convert Entity to DTO
    private UserResponseDTO mapToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setActive(user.isActive());
        return dto;
    }
}