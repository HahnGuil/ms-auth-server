package br.com.hahn.auth.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO for user creation or update requests.")
public record UserRequestDTO(
        @Schema(description = "User's username", example = "john_doe")
        @NotBlank(message = "User Name is required")
        String userName,

        @Schema(description = "User's email address", example = "john.doe@example.com")
        @NotNull(message = "Email is required")
        String email,

        @Schema(description = "User's password", example = "P@ssw0rd!")
        String password,

        @Schema(description = "User's first name", example = "John")
        String firstName,

        @Schema(description = "User's last name", example = "Doe")
        String lastName,

        @Schema(description = "URL of the user's profile picture", example = "https://example.com/images/john.jpg")
        String pictureUrl,

        @Schema(description = "ID of the application associated with the user", example = "1001")
        Long application
) {
}
