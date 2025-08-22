package br.com.hahn.auth.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Defatul error response")
public record ErrorResponseDTO(
        @Schema(description = "Error mesage", example = "Email already registered. Please log in or recover your password")
        String message,

        @Schema(description = "Error Timestamp", example = "2024-01-15T10:30:00Z")
        Instant timestamp
) {
}
