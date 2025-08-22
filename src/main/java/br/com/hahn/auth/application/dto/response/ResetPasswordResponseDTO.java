package br.com.hahn.auth.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO representing the response after a password reset request.")
public record ResetPasswordResponseDTO(
        @Schema(description = "Token used to recover or reset the password", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String recoverToken
) {
}
