package fr.campus.grog.SU.config;

import fr.campus.grog.SU.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that executes once per request to intercept and validate JWT Bearer tokens.
 * If a valid token is present in the Authorization header, it populates the SecurityContextHolder.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Extract the Authorization header
        String authHeader = request.getHeader("Authorization");

        // If header is missing or does not start with "Bearer ", proceed to next filter in the chain
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 2: Strip the "Bearer " prefix (7 characters) to obtain raw compact JWT
        String token = authHeader.substring(7);

        // Step 3: Validate the cryptographic signature and expiration
        if (this.jwtService.isTokenValid(token)) {
            String username = this.jwtService.extractUsername(token);

            // Step 4: If subject is valid and security context has no active authentication yet
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Build authentication token with user authorities
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Feed the SecurityContextHolder so Spring Security recognizes the authenticated user
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 5: Continue filter chain execution
        filterChain.doFilter(request, response);
    }
}
