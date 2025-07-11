package com.fitness.userservice.service;

import com.fitness.userservice.entity.User;
import com.fitness.userservice.model.RegisterRequest;
import com.fitness.userservice.model.UserResponse;
import com.fitness.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse gerUserProfile(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return convertToUserResponse(user);
    }

    public UserResponse register(@Valid RegisterRequest registerRequest) {
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            User existingUser = userRepository.findByEmail(registerRequest.getEmail());
            return convertToUserResponse(existingUser);
        }
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setKeycloakId(registerRequest.getKeycloakId());
        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    private UserResponse convertToUserResponse(User savedUser) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(savedUser.getId());
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setFirstName(savedUser.getFirstName());
        userResponse.setLastName(savedUser.getLastName());
        userResponse.setPassword(savedUser.getPassword());
        userResponse.setRole(savedUser.getRole());
        userResponse.setCreatedAt(savedUser.getCreatedAt());
        userResponse.setUpdatedAt(savedUser.getUpdatedAt());
        userResponse.setKeycloakId(savedUser.getKeycloakId());
        return userResponse;
    }

    public Boolean existByUserId(String userId) {
        return userRepository.existsById(userId);
    }

    public Boolean existByKeyCloakId(String keycloakId) {
        return userRepository.existsByKeycloakId(keycloakId);
    }
}
