package apartment.example.backend.Security;

import apartment.example.backend.entity.User;
import apartment.example.backend.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secretKey = "YourSuperStrongAndRandomSecretKeyForApartmentManagementSystemGoesHere123!@";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // inject secret key & expiration manually
        ReflectionTestUtils.setField(jwtUtil, "secretKey",
                java.util.Base64.getEncoder().encodeToString(secretKey.getBytes()));
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 1000 * 60 * 60L); // 1 hour
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        UserDetails user = new User();
        ((User) user).setUsername("testuser");

        String token = jwtUtil.generateToken(user);
        assertNotNull(token, "Token should not be null");

        String username = jwtUtil.extractUsername(token);
        assertEquals("testuser", username, "Extracted username should match the original");
    }

    @Test
    void testTokenValidity_ValidToken() {
        UserDetails user = new User();
        ((User) user).setUsername("validuser");

        String token = jwtUtil.generateToken(user);
        assertTrue(jwtUtil.isTokenValid(token, user), "Token should be valid for correct user");
    }

    @Test
    void testTokenValidity_InvalidUsername() {
        UserDetails user = new User();
        ((User) user).setUsername("user1");

        String token = jwtUtil.generateToken(user);

        UserDetails anotherUser = new User();
        ((User) anotherUser).setUsername("user2");

        assertFalse(jwtUtil.isTokenValid(token, anotherUser), "Token should be invalid for different username");
    }

    @Test
    void testExtractCustomClaim() {
        UserDetails user = new User();
        ((User) user).setUsername("claimuser");

        String token = jwtUtil.generateToken(user);

        Date expiration = jwtUtil.extractClaim(token, Claims::getExpiration);
        assertNotNull(expiration, "Expiration claim should not be null");
        assertTrue(expiration.after(new Date()), "Expiration should be in the future");
    }
}
