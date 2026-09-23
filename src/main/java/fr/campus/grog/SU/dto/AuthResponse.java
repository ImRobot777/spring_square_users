package fr.campus.grog.SU.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Immutable DTO returned upon successful authentication.
 * Delivers the signed compact JWT and its bearer authorization scheme.
 */
public record AuthResponse(
        @Schema(description = "Compact signed JWT token (RS256)")
        String token,

        @Schema(description = "Token scheme type", example = "Bearer")
        String type
) {
    public AuthResponse(String token) {
        this(token, "Bearer");
    }
}
