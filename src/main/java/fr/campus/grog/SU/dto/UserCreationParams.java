package fr.campus.grog.SU.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreationParams(@NotBlank String pseudo, @NotBlank @Email String email, @NotBlank String password) {
}
