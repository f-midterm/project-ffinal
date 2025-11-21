package apartment.example.backend.service;

import apartment.example.backend.dto.*;
import apartment.example.backend.entity.*;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.entity.enums.TenantStatus;
import apartment.example.backend.repository.*;
import apartment.example.backend.security.JwtUtil;
import apartment.example.backend.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock UserDetailsService userDetailsService;
    @Mock UserRepository userRepository;
    @Mock TenantRepository tenantRepository;
    @Mock LeaseRepository leaseRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;

    @InjectMocks AuthService authService;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("john");
        testUser.setEmail("john@mail.com");
        testUser.setPassword("encoded123");
        testUser.setRole(User.Role.USER);
    }

    // -----------------------------------------------------
    // LOGIN
    // -----------------------------------------------------
    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("123");


        when(userDetailsService.loadUserByUsername("john")).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("USER", response.getRole());
        assertEquals("john", response.getUsername());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    // -----------------------------------------------------
    // REGISTER
    // -----------------------------------------------------
    @Test
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setEmail("mail@mail.com");
        req.setPassword("123");

        when(userRepository.findByUsername("john")).thenReturn(Collections.emptyList());
        when(userRepository.findByEmail("mail@mail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123")).thenReturn("ENC123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userDetailsService.loadUserByUsername("john")).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser)).thenReturn("token123");

        RegisterResponse res = authService.register(req);

        assertEquals("User registered successfully", res.getMessage());
        assertEquals("john", res.getUsername());
        assertEquals("USER", res.getRole());
        assertEquals("token123", res.getToken());
    }

    @Test
    void testRegisterUsernameExists() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setEmail("mail@mail.com");
        req.setPassword("123");
        when(userRepository.findByUsername("john")).thenReturn(List.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> authService.register(req));
    }

    @Test
    void testRegisterEmailExists() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setEmail("mail@mail.com");
        req.setPassword("123");

        when(userRepository.findByUsername("john")).thenReturn(Collections.emptyList());
        when(userRepository.findByEmail("mail@mail.com"))
                .thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> authService.register(req));
    }

    // -----------------------------------------------------
    // CREATE PROFILE
    // -----------------------------------------------------
    @Test
    void testCreateProfileSuccess() {
        CreateProfileRequest req = new CreateProfileRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setPhone("0991112222");
        req.setOccupation("Engineer");
        req.setEmergencyContact("Mom");
        req.setEmergencyPhone("0889994444");


        when(userRepository.findByUsername("john")).thenReturn(List.of(testUser));
        when(tenantRepository.findByEmail("john@mail.com")).thenReturn(Optional.empty());

        Tenant saved = new Tenant();
        saved.setId(10L);
        saved.setFirstName("John");
        saved.setLastName("Doe");
        saved.setEmail("john@mail.com");
        saved.setPhone("099-111-2222");

        when(tenantRepository.save(any(Tenant.class))).thenReturn(saved);

        CreateProfileResponse res = authService.createProfile(req, "john");

        assertEquals(10L, res.getId());

        assertEquals("John", res.getFirstName());
        assertEquals("Doe", res.getLastName());
        assertEquals("john@mail.com", res.getEmail());
        assertEquals("099-111-2222", res.getPhone());
        assertEquals("Profile created successfully", res.getMessage());
    }

    @Test
    void testCreateProfileAlreadyExists() {
        CreateProfileRequest req = new CreateProfileRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setPhone("0991112222");
        req.setOccupation("Engineer");
        req.setEmergencyContact("Mom");
        req.setEmergencyPhone("0889994444");


        when(userRepository.findByUsername("john")).thenReturn(List.of(testUser));
        when(tenantRepository.findByEmail("john@mail.com"))
                .thenReturn(Optional.of(new Tenant()));

        assertThrows(IllegalArgumentException.class, () ->
                authService.createProfile(req, "john"));
    }

    @Test
    void testCreateProfileInvalidPhone() {
        CreateProfileRequest req = new CreateProfileRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setPhone("");
        req.setOccupation("Engineer");
        req.setEmergencyContact("Mom");
        req.setEmergencyPhone("0889994444");


        when(userRepository.findByUsername("john")).thenReturn(List.of(testUser));
        when(tenantRepository.findByEmail("john@mail.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                authService.createProfile(req, "john"));
    }

    @Test
    void testCreateProfileWithToken() {
        CreateProfileRequest req = new CreateProfileRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setPhone("0991112222");
        req.setOccupation("Engineer");
        req.setEmergencyContact("Mom");
        req.setEmergencyPhone("0889994444");


        when(jwtUtil.extractUsername("abc-token")).thenReturn("john");
        when(userRepository.findByUsername("john")).thenReturn(List.of(testUser));
        when(tenantRepository.findByEmail("john@mail.com")).thenReturn(Optional.empty());
        when(tenantRepository.save(any(Tenant.class))).thenReturn(new Tenant());

        assertDoesNotThrow(() ->
                authService.createProfileWithToken(req, "abc-token"));
    }

    // -----------------------------------------------------
    // GET CURRENT USER PROFILE
    // -----------------------------------------------------
    @Test
    void testGetCurrentUserProfile_UserOnly() {
        when(jwtUtil.extractUsername("token123")).thenReturn("john");
        when(userRepository.findByUsername("john")).thenReturn(List.of(testUser));
        when(tenantRepository.findByEmail("john@mail.com")).thenReturn(Optional.empty());

        UserProfileDto profile = authService.getCurrentUserProfile("token123");

        assertEquals(1L, profile.getId());
        assertEquals("john", profile.getUsername());
        assertEquals("john@mail.com", profile.getEmail());
        assertEquals("USER", profile.getRole());
        assertNull(profile.getTenantId());
    }

    @Test
    void testGetCurrentUserProfile_UserTenantLease() {
        Tenant t = new Tenant();
        t.setId(99L);
        t.setFirstName("John");
        t.setLastName("Doe");
        t.setPhone("0999999999");

        Unit unit = new Unit();
        unit.setId(555L);

        Lease lease = new Lease();
        lease.setTenant(t);
        lease.setStatus(LeaseStatus.ACTIVE);
        lease.setUnit(unit);

        when(jwtUtil.extractUsername("token123")).thenReturn("john");
        when(userRepository.findByUsername("john")).thenReturn(List.of(testUser));
        when(tenantRepository.findByEmail("john@mail.com")).thenReturn(Optional.of(t));
        when(leaseRepository.findByTenantIdAndStatus(99L, LeaseStatus.ACTIVE)).thenReturn(List.of(lease));

        UserProfileDto profile = authService.getCurrentUserProfile("token123");

        assertEquals(99L, profile.getTenantId());
        assertEquals(555L, profile.getUnitId());
    }
}
