package br.com.hahn.auth.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "DTO representing the response with user details.")
public record UserResponseDTO(
        @Schema(description = "Unique identifier of the user", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
        UUID userId,

        @Schema(description = "User's username", example = "john_doe")
        String userName,

        @Schema(description = "User's email address", example = "john.doe@example.com")
        String email,

        @Schema(description = "User's first name", example = "John")
        String firstName,

        @Schema(description = "User's last name", example = "Doe")
        String lastName,

        @Schema(description = "URL of the user's profile picture", example = "https://example.com/images/john.jpg")
        String pictureUrl,

        @Schema(description = "JWT access token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token
) {

    public UserResponseDTO(String userName, String email, String token) {
        this(null, userName, email, null, null, null, token);
    }

}
