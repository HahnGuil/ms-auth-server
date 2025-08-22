package br.com.hahn.auth.application.dto.response;

import br.com.hahn.auth.domain.enums.ScopeToken;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "DTO representing a user's login log entry.")
public record LoginLogResponseDTO(
        @Schema(description = "Unique identifier for the login log entry", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
        UUID loginLogID,

        @Schema(description = "Scope of the token used during login", example = "USER")
        ScopeToken scopeToken,

        @Schema(description = "Date and time of the login event", example = "2024-06-01T12:34:56")
        LocalDateTime dateLogin
) {
}
