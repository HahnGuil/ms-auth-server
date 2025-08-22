package br.com.hahn.auth.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO representing the response after a successful user login.")
public record LoginResponseDTO(
        @Schema(description = "User's email address", example = "user@example.com")
        String email,

        @Schema(description = "JWT access token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "JWT refresh token for renewing authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken
) {
}
