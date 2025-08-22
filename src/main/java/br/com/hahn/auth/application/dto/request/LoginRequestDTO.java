package br.com.hahn.auth.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for user login request containing email and password.")
public record LoginRequestDTO(
        @Schema(description = "User's email address", example = "user@example.com")
        String email,

        @Schema(description = "User's password", example = "P@ssw0rd!")
        String password
) {
}
