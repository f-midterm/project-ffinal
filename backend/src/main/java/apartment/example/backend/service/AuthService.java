package apartment.example.backend.service;

import apartment.example.backend.dto.LoginRequest;
import apartment.example.backend.dto.LoginResponse;
import apartment.example.backend.dto.RegisterRequest;
import apartment.example.backend.dto.RegisterResponse;
import apartment.example.backend.entity.User;
import apartment.example.backend.repository.UserRepository;
import apartment.example.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for authentication and user management.
 * Handles login, registration, and JWT token generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Authenticate user and generate JWT token
     * @param loginRequest Contains username and password
     * @return LoginResponse with JWT token, role, and username
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     */
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        
        // Authenticate user credentials
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
            )
        );
        
        // Load user details
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        final User user = (User) userDetails;
        
        // Generate JWT token
        final String token = jwtUtil.generateToken(userDetails);
        
        log.info("User logged in successfully: {}", user.getUsername());
        return new LoginResponse(token, user.getRole().toString(), user.getUsername());
    }

    /**
     * Register a new user in the system
     * @param registerRequest Contains username, email, and password
     * @return RegisterResponse with success message and username
     * @throws IllegalArgumentException if username or email already exists
     */
    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        log.info("Registration attempt for username: {}", registerRequest.getUsername());
        
        // Validate username uniqueness
        if (!userRepository.findByUsername(registerRequest.getUsername()).isEmpty()) {
            log.warn("Registration failed: Username already exists - {}", registerRequest.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Validate email uniqueness
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            log.warn("Registration failed: Email already exists - {}", registerRequest.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Create new user
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setEmail(registerRequest.getEmail());
        newUser.setRole(User.Role.USER); // Default role for new users
        
        // Save to database
        User savedUser = userRepository.save(newUser);
        log.info("User registered successfully: {}", savedUser.getUsername());
        
        return new RegisterResponse("User registered successfully", savedUser.getUsername());
    }
}
