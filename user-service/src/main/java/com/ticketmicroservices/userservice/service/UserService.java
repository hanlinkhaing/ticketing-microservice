package com.ticketmicroservices.userservice.service;

import com.ticketmicroservices.userservice.dto.AuthResponse;
import com.ticketmicroservices.userservice.dto.LoginRequest;
import com.ticketmicroservices.userservice.dto.RegisterRequest;
import com.ticketmicroservices.userservice.dto.UserDto;
import com.ticketmicroservices.userservice.entity.User;
import com.ticketmicroservices.userservice.repository.UserRepository;
import com.ticketmicroservices.userservice.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User(
            registerRequest.getUsername(),
            registerRequest.getEmail(),
            passwordEncoder.encode(registerRequest.getPassword()),
            registerRequest.getFullName(),
            User.Role.USER
        );

        user = userRepository.save(user);

        String token = tokenProvider.generateToken(user.getUsername(), user.getRole().toString());

        // Send user created event to Kafka
        kafkaTemplate.send("user-events", "USER_CREATED", user.getId().toString());

        return new AuthResponse(token, user.getUsername(), user.getRole().toString());
    }

    public AuthResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = tokenProvider.generateToken(user.getUsername(), user.getRole().toString());

        return new AuthResponse(token, user.getUsername(), user.getRole().toString());
    }

    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getPhoneNumber(),
            user.getRole().toString()
        );
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getPhoneNumber(),
            user.getRole().toString()
        );
    }
}