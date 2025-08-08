package br.com.hahn.auth.application.dto.response;

import br.com.hahn.auth.domain.enums.ScopeToken;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoginLogResponseDTO(UUID loginLogID, ScopeToken scopeToken, LocalDateTime dateLogin) {
}
