package apartment.example.backend.controller;

import apartment.example.backend.dto.*;
import apartment.example.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_shouldReturnOkResponse() {
        LoginRequest req = new LoginRequest();
        req.setUsername("user");
        req.setPassword("pass");

        LoginResponse mockResp = new LoginResponse("token123", "USER", "user");
        when(authService.login(req)).thenReturn(mockResp);

        ResponseEntity<LoginResponse> response = authController.login(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResp, response.getBody());
    }

    @Test
    void register_shouldReturnOkResponse() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");

        RegisterResponse mockResp = new RegisterResponse("registeredUser", "newuser", "token123", "USER");
        when(authService.register(req)).thenReturn(mockResp);

        ResponseEntity<RegisterResponse> response = authController.register(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResp, response.getBody());
    }

    @Test
    void createProfile_missingAuthHeader_shouldReturnUnauthorized() {
        CreateProfileRequest req = new CreateProfileRequest();

        ResponseEntity<CreateProfileResponse> response = authController.createProfile(req, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void createProfile_invalidAuthHeaderFormat_shouldReturnUnauthorized() {
        CreateProfileRequest req = new CreateProfileRequest();

        ResponseEntity<CreateProfileResponse> response = authController.createProfile(req, "InvalidToken");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void createProfile_validToken_shouldReturnCreated() {
        CreateProfileRequest req = new CreateProfileRequest();
        String token = "valid.jwt.token";
        String header = "Bearer " + token;

        CreateProfileResponse mockResp = new CreateProfileResponse();
        when(authService.createProfileWithToken(req, token)).thenReturn(mockResp);

        ResponseEntity<CreateProfileResponse> response = authController.createProfile(req, header);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResp, response.getBody());
    }

    @Test
    void getCurrentUser_missingAuthHeader_shouldReturnUnauthorized() {
        ResponseEntity<UserProfileDto> response = authController.getCurrentUser(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getCurrentUser_invalidHeaderFormat_shouldReturnUnauthorized() {
        ResponseEntity<UserProfileDto> response = authController.getCurrentUser("Token abc");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getCurrentUser_validToken_shouldReturnOk() {
        String token = "valid.jwt.token";
        String header = "Bearer " + token;

        UserProfileDto mockProfile = new UserProfileDto();
        when(authService.getCurrentUserProfile(token)).thenReturn(mockProfile);

        ResponseEntity<UserProfileDto> response = authController.getCurrentUser(header);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockProfile, response.getBody());
    }
}
