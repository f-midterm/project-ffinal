package apartment.example.backend.controller;

import apartment.example.backend.dto.LoginRequest;
import apartment.example.backend.dto.LoginResponse;
import apartment.example.backend.dto.RegisterRequest;
import apartment.example.backend.dto.RegisterResponse;
import apartment.example.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController
 * Tests login and registration endpoints
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    /**
     * Test Exception Handler for handling exceptions in tests
     */
    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
            return ResponseEntity.status(400).body(ex.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        LoginResponse expectedResponse = new LoginResponse(
                "jwt-token-123",
                "USER",
                "testuser"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return 401 when login with invalid credentials")
    void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should login admin user with ADMIN role")
    void testLogin_AdminUser() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("adminpass");

        LoginResponse expectedResponse = new LoginResponse(
                "jwt-admin-token",
                "ADMIN",
                "admin"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.username").value("admin"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should test login controller direct method call - Success")
    void testLogin_DirectMethodCall_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        LoginResponse expectedResponse = new LoginResponse(
                "jwt-token-123",
                "USER",
                "testuser"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<LoginResponse> response = authController.login(loginRequest);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("jwt-token-123");
        assertThat(response.getBody().getRole()).isEqualTo("USER");
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when login with invalid credentials - Direct call")
    void testLogin_DirectMethodCall_InvalidCredentials() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authController.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    // ==================== REGISTER TESTS ====================

    @Test
    @DisplayName("Should successfully register new user")
    void testRegister_Success() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("newuser@example.com");

        RegisterResponse expectedResponse = new RegisterResponse(
                "User registered successfully",
                "newuser"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.username").value("newuser"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when username already exists")
    void testRegister_UsernameExists() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("existinguser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("user@example.com");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when email already exists")
    void testRegister_EmailExists() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("existing@example.com");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should test register controller direct method call - Success")
    void testRegister_DirectMethodCall_Success() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("newuser@example.com");

        RegisterResponse expectedResponse = new RegisterResponse(
                "User registered successfully",
                "newuser"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<RegisterResponse> response = authController.register(registerRequest);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("User registered successfully");
        assertThat(response.getBody().getUsername()).isEqualTo("newuser");

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when username exists - Direct call")
    void testRegister_DirectMethodCall_UsernameExists() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("existinguser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("user@example.com");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        // Act & Assert
        assertThatThrownBy(() -> authController.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when email exists - Direct call")
    void testRegister_DirectMethodCall_EmailExists() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("existing@example.com");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        // Act & Assert
        assertThatThrownBy(() -> authController.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should handle registration with all valid fields")
    void testRegister_AllFieldsValid() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("validuser");
        registerRequest.setPassword("StrongP@ssw0rd");
        registerRequest.setEmail("valid@example.com");

        RegisterResponse expectedResponse = new RegisterResponse(
                "User registered successfully",
                "validuser"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.username").value("validuser"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should verify login service is called with correct request")
    void testLogin_VerifyServiceCall() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        LoginResponse expectedResponse = new LoginResponse("token", "USER", "testuser");
        when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        // Act
        authController.login(loginRequest);

        // Assert
        verify(authService).login(argThat(request ->
                request.getUsername().equals("testuser") &&
                        request.getPassword().equals("password123")
        ));
    }

    @Test
    @DisplayName("Should verify register service is called with correct request")
    void testRegister_VerifyServiceCall() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("new@example.com");

        RegisterResponse expectedResponse = new RegisterResponse("Success", "newuser");
        when(authService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);

        // Act
        authController.register(registerRequest);

        // Assert
        verify(authService).register(argThat(request ->
                request.getUsername().equals("newuser") &&
                        request.getPassword().equals("password123") &&
                        request.getEmail().equals("new@example.com")
        ));
    }
}