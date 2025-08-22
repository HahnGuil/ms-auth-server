package br.com.hahn.auth.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for password operations such as change or recovery.")
public record PasswordOperationRequestDTO(
        @Schema(description = "User's email address", example = "user@example.com")
        String email,

        @Schema(description = "User's current password", example = "OldP@ssw0rd!")
        String oldPassword,

        @Schema(description = "User's new password", example = "NewP@ssw0rd!")
        String newPassword,

        @Schema(description = "Recovery code for password reset", example = "123456")
        String recoverCode
) {
}
