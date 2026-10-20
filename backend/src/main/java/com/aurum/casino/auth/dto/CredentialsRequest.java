package com.aurum.casino.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CredentialsRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_]{3,20}$", message = "debe tener 3-20 caracteres: letras, números o _")
        String username,

        @NotBlank
        @Size(min = 6, max = 72, message = "debe tener entre 6 y 72 caracteres")
        String password) {
}
