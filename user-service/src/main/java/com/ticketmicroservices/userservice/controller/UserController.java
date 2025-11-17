package com.ticketmicroservices.userservice.controller;

import com.ticketmicroservices.userservice.dto.AuthResponse;
import com.ticketmicroservices.userservice.dto.LoginRequest;
import com.ticketmicroservices.userservice.dto.RegisterRequest;
import com.ticketmicroservices.userservice.dto.UserDto;
import com.ticketmicroservices.userservice.security.JwtTokenProvider;
import com.ticketmicroservices.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final String SERVICE_NAME = "user-service";

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        String requestId = java.util.UUID.randomUUID().toString();
        logger.info("[{}] [REGISTER] [START] Request received: username={}, email={}", requestId, registerRequest.getUsername(), registerRequest.getEmail());
        
        try {
            // Test error scenarios - remove this in production
            if ("test-error".equals(registerRequest.getUsername())) {
                logger.error("[{}] [REGISTER] [TEST] Simulating internal server error", requestId);
                throw new RuntimeException("Simulated internal server error for testing");
            }
            
            if ("test-validation".equals(registerRequest.getUsername())) {
                logger.error("[{}] [REGISTER] [TEST] Simulating validation error", requestId);
                return ResponseEntity.badRequest().body(createErrorResponse("Test validation error - username not allowed", requestId));
            }
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                logger.error("[{}] [REGISTER] [ERROR] Username is required", requestId);
                return ResponseEntity.badRequest().body(createErrorResponse("Username is required", requestId));
            }
            
            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                logger.error("[{}] [REGISTER] [ERROR] Email is required", requestId);
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required", requestId));
            }
            
            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
                logger.error("[{}] [REGISTER] [ERROR] Password is required", requestId);
                return ResponseEntity.badRequest().body(createErrorResponse("Password is required", requestId));
            }
            
            logger.info("[{}] [REGISTER] [PROCESSING] Calling user service", requestId);
            AuthResponse response = userService.register(registerRequest);
            logger.info("[{}] [REGISTER] [SUCCESS] User registered successfully: username={}", requestId, registerRequest.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("[{}] [REGISTER] [ERROR] Runtime exception: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(createErrorResponse(e.getMessage(), requestId));
        } catch (Exception e) {
            logger.error("[{}] [REGISTER] [ERROR] Unexpected exception: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred during registration", requestId));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String requestId = java.util.UUID.randomUUID().toString();
        logger.info("[{}] [LOGIN] [START] Login request received: username={}", requestId, loginRequest.getUsername());
        
        try {
            if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                logger.error("[{}] [LOGIN] [ERROR] Username is required", requestId);
                return ResponseEntity.badRequest().body(createErrorResponse("Username is required", requestId));
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                logger.error("[{}] [LOGIN] [ERROR] Password is required", requestId);
                return ResponseEntity.badRequest().body(createErrorResponse("Password is required", requestId));
            }
            
            logger.info("[{}] [LOGIN] [PROCESSING] Calling user service", requestId);
            AuthResponse response = userService.login(loginRequest);
            logger.info("[{}] [LOGIN] [SUCCESS] User logged in successfully: username={}", requestId, loginRequest.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("[{}] [LOGIN] [ERROR] Authentication failed: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse("Invalid username or password", requestId));
        } catch (Exception e) {
            logger.error("[{}] [LOGIN] [ERROR] Unexpected exception: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred during login", requestId));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(
            @RequestParam(value = "username", required = false) String usernameParam,
            @RequestHeader(value = "x-user-id", required = false) String userIdHeader,
            @RequestHeader(value = "x-user-name", required = false) String userNameHeader,
            HttpServletRequest request) {
        String requestId = java.util.UUID.randomUUID().toString();
        String tokenUsername = extractUsernameFromToken(request.getHeader("Authorization"));
        String resolvedUsername = firstNonEmpty(usernameParam, userNameHeader, userIdHeader, tokenUsername);
        logger.info("[{}] [PROFILE] [START] Get current user profile request: username={}", requestId, resolvedUsername);

        try {
            if (resolvedUsername == null && tokenUsername == null) {
                logger.error("[{}] [PROFILE] [ERROR] Username is required via token, query param, or header", requestId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse("Valid token or username is required", requestId));
            }

            if (resolvedUsername == null) {
                logger.error("[{}] [PROFILE] [ERROR] Could not resolve username from token", requestId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse("Invalid or missing token", requestId));
            }

            logger.info("[{}] [PROFILE] [PROCESSING] Calling user service", requestId);
            UserDto userDto = userService.getUserByUsername(resolvedUsername);
            logger.info("[{}] [PROFILE] [SUCCESS] User profile retrieved: username={}", requestId, resolvedUsername);

            return ResponseEntity.ok(userDto);

        } catch (RuntimeException e) {
            logger.error("[{}] [PROFILE] [ERROR] User not found: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage(), requestId));
        } catch (Exception e) {
            logger.error("[{}] [PROFILE] [ERROR] Unexpected exception: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred while retrieving user profile", requestId));
        }
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        String requestId = java.util.UUID.randomUUID().toString();
        logger.info("[{}] [PROFILE] [START] Get user profile request: username={}", requestId, username);
        
        try {
            if (username == null || username.trim().isEmpty()) {
                logger.error("[{}] [PROFILE] [ERROR] Username parameter is required", requestId);
                return ResponseEntity.badRequest().body(createErrorResponse("Username is required", requestId));
            }
            
            logger.info("[{}] [PROFILE] [PROCESSING] Calling user service", requestId);
            UserDto userDto = userService.getUserByUsername(username);
            logger.info("[{}] [PROFILE] [SUCCESS] User profile retrieved: username={}", requestId, username);
            
            return ResponseEntity.ok(userDto);
            
        } catch (RuntimeException e) {
            logger.error("[{}] [PROFILE] [ERROR] User not found: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage(), requestId));
        } catch (Exception e) {
            logger.error("[{}] [PROFILE] [ERROR] Unexpected exception: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred while retrieving user profile", requestId));
        }
    }

    @GetMapping({"/id/{id}", "/{id:\\d+}"})
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        String requestId = java.util.UUID.randomUUID().toString();
        logger.info("[{}] [GET_BY_ID] [START] Get user by ID request: id={}", requestId, id);
        
        try {
            if (id == null || id <= 0) {
                logger.error("[{}] [GET_BY_ID] [ERROR] Invalid user ID: {}", requestId, id);
                return ResponseEntity.badRequest().body(createErrorResponse("Valid user ID is required", requestId));
            }
            
            logger.info("[{}] [GET_BY_ID] [PROCESSING] Calling user service", requestId);
            UserDto userDto = userService.getUserById(id);
            logger.info("[{}] [GET_BY_ID] [SUCCESS] User retrieved by ID: id={}", requestId, id);
            
            return ResponseEntity.ok(userDto);
            
        } catch (RuntimeException e) {
            logger.error("[{}] [GET_BY_ID] [ERROR] User not found: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage(), requestId));
        } catch (Exception e) {
            logger.error("[{}] [GET_BY_ID] [ERROR] Unexpected exception: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred while retrieving user", requestId));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        String requestId = java.util.UUID.randomUUID().toString();
        logger.info("[{}] [HEALTH] [START] Health check request received", requestId);
        
        try {
            Map<String, Object> healthResponse = new HashMap<>();
            healthResponse.put("status", "UP");
            healthResponse.put("service", SERVICE_NAME);
            healthResponse.put("timestamp", System.currentTimeMillis());
            healthResponse.put("requestId", requestId);
            
            logger.info("[{}] [HEALTH] [SUCCESS] Health check completed successfully", requestId);
            return ResponseEntity.ok(healthResponse);
            
        } catch (Exception e) {
            logger.error("[{}] [HEALTH] [ERROR] Health check failed: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Health check failed", requestId));
        }
    }
    
    private Map<String, Object> createErrorResponse(String message, String requestId) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("message", message);
        errorResponse.put("service", SERVICE_NAME);
        errorResponse.put("requestId", requestId);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private String extractUsernameFromToken(String authHeader) {
        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return null;
        }
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return null;
        }
        try {
            if (!tokenProvider.validateToken(token)) {
                return null;
            }
            return tokenProvider.getUsernameFromToken(token);
        } catch (Exception e) {
            logger.error("[TOKEN] Failed to parse token: {}", e.getMessage());
            return null;
        }
    }
}
