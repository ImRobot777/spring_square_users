package fr.campus.grog.SU.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Step 1: Disable CSRF protection (unnecessary for stateless REST APIs)
        http.csrf(csrf -> csrf.disable());

        // Step 2: Set session creation policy to STATELESS (no HTTP session in RAM)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Step 3: Define URL authorization rules
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/users").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated()
        );

        // Step 4: Register custom JWT authentication filter before the username/password filter
        http.addFilterBefore(this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
