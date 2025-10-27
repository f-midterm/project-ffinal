package apartment.example.backend.Service;

import apartment.example.backend.dto.LoginRequest;
import apartment.example.backend.dto.LoginResponse;
import apartment.example.backend.dto.RegisterRequest;
import apartment.example.backend.dto.RegisterResponse;
import apartment.example.backend.entity.User;
import apartment.example.backend.repository.UserRepository;
import apartment.example.backend.security.JwtUtil;
import apartment.example.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

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
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("john");
        mockUser.setEmail("john@example.com");
        mockUser.setPassword("encodedPass");
        mockUser.setRole(User.Role.USER);
    }

    // --------------------------
    // LOGIN TESTS
    // --------------------------
    @Test
    void login_ShouldReturnLoginResponse_WhenCredentialsAreValid() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password123");

        when(userDetailsService.loadUserByUsername("john")).thenReturn(mockUser);
        when(jwtUtil.generateToken(mockUser)).thenReturn("fake-jwt-token");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("USER", response.getRole());
        assertEquals("john", response.getUsername());
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("john", "password123")
        );
    }

    @Test
    void login_ShouldThrowBadCredentials_WhenAuthenticationFails() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("wrongUser");
        request.setPassword("wrongPass");

        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    // --------------------------
    // REGISTER TESTS
    // --------------------------
    @Test
    void register_ShouldCreateUser_WhenDataIsValid() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("new@example.com");
        request.setPassword("123456");

        when(userRepository.findByUsername("newUser")).thenReturn(List.of());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("encoded123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("User registered successfully", response.getMessage());
        assertEquals("newUser", response.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange

        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john2@example.com");
        request.setPassword("123456");
        when(userRepository.findByUsername("john")).thenReturn(List.of(mockUser));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));

        assertEquals("Username already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("john@example.com");
        request.setPassword("123456");
        when(userRepository.findByUsername("newUser")).thenReturn(List.of());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));

        assertEquals("Email already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }
}
