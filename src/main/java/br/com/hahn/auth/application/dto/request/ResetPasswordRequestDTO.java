package br.com.hahn.auth.application.dto.request;

public record ResetPasswordRequestDTO(String email, String recorverCode) {
}
