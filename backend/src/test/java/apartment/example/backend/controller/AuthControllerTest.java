package apartment.example.backend.controller;

import apartment.example.backend.dto.LoginRequest;
import apartment.example.backend.dto.LoginResponse;
import apartment.example.backend.dto.RegisterRequest;
import apartment.example.backend.dto.RegisterResponse;
import apartment.example.backend.entity.User;
import apartment.example.backend.repository.UserRepository;
import apartment.example.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private User mockUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // สร้าง mock user
        mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPassword");
        mockUser.setEmail("test@example.com");
        mockUser.setRole(User.Role.USER);

        // สร้าง login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // สร้าง register request
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("newuser@example.com");
    }

    // ========== Login Tests ==========

    @Test
    void testLogin_Success() {
        // Given
        String expectedToken = "mock.jwt.token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailsService.loadUserByUsername(loginRequest.getUsername()))
                .thenReturn(mockUser);
        when(jwtUtil.generateToken(mockUser))
                .thenReturn(expectedToken);

        // When
        ResponseEntity<?> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof LoginResponse);

        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertEquals(expectedToken, loginResponse.getToken());
        assertEquals("USER", loginResponse.getRole());
        assertEquals("testuser", loginResponse.getUsername());

        // Verify
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername(loginRequest.getUsername());
        verify(jwtUtil, times(1)).generateToken(mockUser);
    }

    @Test
    void testLogin_BadCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When
        ResponseEntity<?> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Incorrect username or password", response.getBody());

        // Verify
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void testLogin_WithAdminRole() {
        // Given
        mockUser.setRole(User.Role.ADMIN);
        String expectedToken = "mock.jwt.token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailsService.loadUserByUsername(loginRequest.getUsername()))
                .thenReturn(mockUser);
        when(jwtUtil.generateToken(mockUser))
                .thenReturn(expectedToken);

        // When
        ResponseEntity<?> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertEquals("ADMIN", loginResponse.getRole());
    }

    // ========== Register Tests ==========

    @Test
    void testRegister_Success() {
        // Given
        when(userRepository.findByUsername(registerRequest.getUsername()))
                .thenReturn(Collections.emptyList());
        when(userRepository.findByEmail(registerRequest.getEmail()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword()))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ResponseEntity<?> response = authController.register(registerRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof RegisterResponse);

        RegisterResponse registerResponse = (RegisterResponse) response.getBody();
        assertEquals("User registered successfully", registerResponse.getMessage());
        assertEquals("newuser", registerResponse.getUsername());

        // Verify
        verify(userRepository, times(1)).findByUsername(registerRequest.getUsername());
        verify(userRepository, times(1)).findByEmail(registerRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        // Given
        when(userRepository.findByUsername(registerRequest.getUsername()))
                .thenReturn(Collections.singletonList(mockUser));

        // When
        ResponseEntity<?> response = authController.register(registerRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username already exists", response.getBody());

        // Verify
        verify(userRepository, times(1)).findByUsername(registerRequest.getUsername());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Given
        when(userRepository.findByUsername(registerRequest.getUsername()))
                .thenReturn(Collections.emptyList());
        when(userRepository.findByEmail(registerRequest.getEmail()))
                .thenReturn(Optional.of(mockUser));

        // When
        ResponseEntity<?> response = authController.register(registerRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already exists", response.getBody());

        // Verify
        verify(userRepository, times(1)).findByUsername(registerRequest.getUsername());
        verify(userRepository, times(1)).findByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_DefaultRoleIsUser() {
        // Given
        when(userRepository.findByUsername(registerRequest.getUsername()))
                .thenReturn(Collections.emptyList());
        when(userRepository.findByEmail(registerRequest.getEmail()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword()))
                .thenReturn("encodedPassword");

        // Capture the user being saved
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    assertEquals(User.Role.USER, user.getRole());
                    return user;
                });

        // When
        authController.register(registerRequest);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_Exception() {
        // Given
        when(userRepository.findByUsername(registerRequest.getUsername()))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = authController.register(registerRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Registration failed"));

        // Verify
        verify(userRepository, times(1)).findByUsername(registerRequest.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_PasswordIsEncoded() {
        // Given
        String rawPassword = "plainPassword123";
        String encodedPassword = "encodedPassword123";
        registerRequest.setPassword(rawPassword);

        when(userRepository.findByUsername(registerRequest.getUsername()))
                .thenReturn(Collections.emptyList());
        when(userRepository.findByEmail(registerRequest.getEmail()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword))
                .thenReturn(encodedPassword);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    assertEquals(encodedPassword, user.getPassword());
                    assertNotEquals(rawPassword, user.getPassword());
                    return user;
                });

        // When
        authController.register(registerRequest);

        // Then
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }
}