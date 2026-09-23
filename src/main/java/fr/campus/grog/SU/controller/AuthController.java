package fr.campus.grog.SU.controller;

import fr.campus.grog.SU.dto.AuthResponse;
import fr.campus.grog.SU.dto.LoginRequest;
import fr.campus.grog.SU.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@Tag(name = "Authentification", description = "Endpoints d'authentification et émission de jetons JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Connexion utilisateur", description = "Vérifie les identifiants et génère un jeton JWT asymétrique (RS256).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentification réussie, jeton JWT retourné"),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides (pseudo ou mot de passe incorrect)")
    })
    @PostMapping("/auth/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        try {
            // Step 1: Delegate authentication to Spring Security's AuthenticationManager
            Authentication authentication = this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            // Step 2: Extract granted authorities/roles from authenticated principal
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            // Step 3: Generate signed JWT token using RSA private key
            String token = this.jwtService.generateToken(authentication.getName(), roles);

            return new AuthResponse(token, "Bearer");
        } catch (AuthenticationException e) {
            // Catches BadCredentialsException, UsernameNotFoundException, etc., and returns 401 UNAUTHORIZED
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
    }
}
