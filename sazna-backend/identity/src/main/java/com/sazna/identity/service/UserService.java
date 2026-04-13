package com.sazna.identity.service;

import com.sazna.identity.dto.*;
import com.sazna.identity.entity.User;
import com.sazna.identity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDTO syncWithProvider(ProviderSyncRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .map(existingUser -> {
                    // Update info if it changed on the provider side
                    existingUser.setFirstName(request.getFirstName());
                    existingUser.setLastName(request.getLastName());
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    // Auto-create user if they don't exist
                    User newUser = new User();
                    newUser.setEmail(request.getEmail());
                    newUser.setFirstName(request.getFirstName());
                    newUser.setLastName(request.getLastName());
                    newUser.setActive(true);
                    // Standard practice: mark as OAUTH so we know they don't have a local password
                    newUser.setPassword("EXTERNAL_AUTH_PROVIDER");
                    return userRepository.save(newUser);
                });
        return mapToDTO(user);
    }

    public UserResponseDTO getProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDTO(user);
    }

    @Transactional
    public UserResponseDTO updateProfile(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        return mapToDTO(userRepository.save(user));
    }

    @Transactional
    public void deactivateUser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setActive(false);
            userRepository.save(user);
        });
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
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

    @Transactional
    public UserResponseDTO registerLocalUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with this email already exists.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return mapToDTO(userRepository.save(user));
    }

    public ValidateUserResponse validateUser(ValidateUserRequest request) {

        return userRepository.findByEmail(request.getEmail())
                .map(user -> validatePassword(user, request.getPassword()))
                .orElse(new ValidateUserResponse(false, null, null));
    }

    private ValidateUserResponse validatePassword(User user, String rawPassword) {

        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());

        if (matches) {
            return new ValidateUserResponse(true, user.getId(), user.getEmail());
        }

        return new ValidateUserResponse(false, null, null);
    }
}