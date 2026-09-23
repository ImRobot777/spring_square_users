package fr.campus.grog.SU.controller;

import fr.campus.grog.SU.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(this.authenticationManager, this.jwtService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testLogin_WithValidCredentials_ReturnsHttp200AndToken() throws Exception {
        // --- 1. ARRANGE ---
        Authentication fakeAuth = mock(Authentication.class);
        when(fakeAuth.getName()).thenReturn("Alice");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(fakeAuth).getAuthorities();

        when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(fakeAuth);

        when(this.jwtService.generateToken("Alice", List.of("ROLE_USER")))
                .thenReturn("mocked.jwt.token");

        String requestBody = """
        {
            "username": "Alice",
            "password": "secretPassword123"
        }
        """;

        // --- 2 & 3. ACT & ASSERT ---
        this.mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.type").value("Bearer"));

        verify(this.authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(this.jwtService, times(1)).generateToken("Alice", List.of("ROLE_USER"));
    }

    @Test
    void testLogin_WithInvalidCredentials_ReturnsHttp401Unauthorized() throws Exception {
        // --- 1. ARRANGE ---
        when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        String requestBody = """
        {
            "username": "Alice",
            "password": "wrongPassword"
        }
        """;

        // --- 2 & 3. ACT & ASSERT ---
        this.mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());

        verify(this.authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(this.jwtService);
    }
}
