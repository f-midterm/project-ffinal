package apartment.example.backend.controller;

import apartment.example.backend.dto.LoginRequest;
import apartment.example.backend.dto.LoginResponse;
import apartment.example.backend.dto.RegisterRequest;
import apartment.example.backend.dto.RegisterResponse;
import apartment.example.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints.
 * Handles user login and registration.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint - authenticates user and returns JWT token
     * @param loginRequest Contains username and password
     * @return JWT token with user information
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login request received for user: {}", loginRequest.getUsername());
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Registration endpoint - creates new user account
     * @param registerRequest Contains username, email, and password
     * @return Success message with username
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        log.info("Registration request received for username: {}", registerRequest.getUsername());
        RegisterResponse response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }
}