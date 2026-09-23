package fr.campus.grog.SU.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        // Instantiate the real JwtService
        this.jwtService = new JwtService();

        // Inject the RSA keys from classpath resources using Spring's ReflectionTestUtils
        ReflectionTestUtils.setField(this.jwtService, "privateKeyResource", new ClassPathResource("certs/private_key.pem"));
        ReflectionTestUtils.setField(this.jwtService, "publicKeyResource", new ClassPathResource("certs/public_key.pem"));

        // Initialize and decode the cryptographic keys
        this.jwtService.init();
    }

    @Test
    void testGenerateToken_AndExtractUsername_ReturnsCorrectUsername() {
        // --- 1. ARRANGE ---
        String username = "Alice";
        List<String> roles = List.of("ROLE_USER");

        // --- 2. ACT ---
        String token = this.jwtService.generateToken(username, roles);
        String extractedUsername = this.jwtService.extractUsername(token);

        // --- 3. ASSERT ---
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("Alice", extractedUsername);
    }

    @Test
    void testIsTokenValid_WithFreshValidToken_ReturnsTrue() {
        // --- 1. ARRANGE ---
        String token = this.jwtService.generateToken("Bob", List.of("ROLE_USER", "ROLE_ADMIN"));

        // --- 2. ACT ---
        boolean isValid = this.jwtService.isTokenValid(token);

        // --- 3. ASSERT ---
        assertTrue(isValid);
    }

    @Test
    void testIsTokenValid_WithTamperedToken_ReturnsFalse() {
        // --- 1. ARRANGE ---
        String validToken = this.jwtService.generateToken("Charlie", List.of("ROLE_USER"));
        // Tamper with the cryptographic signature by changing characters at the end
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "abcde";

        // --- 2. ACT ---
        boolean isValid = this.jwtService.isTokenValid(tamperedToken);

        // --- 3. ASSERT ---
        assertFalse(isValid);
    }

    @Test
    void testIsTokenValid_WithMalformedString_ReturnsFalse() {
        // --- 1. ARRANGE ---
        String malformedToken = "invalid.token.structure";

        // --- 2. ACT ---
        boolean isValid = this.jwtService.isTokenValid(malformedToken);

        // --- 3. ASSERT ---
        assertFalse(isValid);
    }

    @Test
    void testIsTokenValid_WithNullOrBlankString_ReturnsFalse() {
        // --- 1. ACT & ASSERT ---
        assertFalse(this.jwtService.isTokenValid(null));
        assertFalse(this.jwtService.isTokenValid(""));
    }
}
