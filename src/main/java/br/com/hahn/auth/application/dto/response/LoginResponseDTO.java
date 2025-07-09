package br.com.hahn.auth.application.dto.response;

public record LoginResponseDTO(String email, String token, String refreshToken) {

}
