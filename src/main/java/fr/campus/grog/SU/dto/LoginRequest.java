package fr.campus.grog.SU.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Immutable DTO representing credentials submitted to POST /auth/login.
 * Supports both "username" and "pseudo" JSON field keys transparently via @JsonAlias.
 */
public record LoginRequest(
        @Schema(description = "User unique pseudo or username", example = "Alice")
        @JsonAlias("pseudo")
        @NotBlank
        String username,

        @Schema(description = "User raw password", example = "secretPassword123")
        @NotBlank
        String password
) {}
