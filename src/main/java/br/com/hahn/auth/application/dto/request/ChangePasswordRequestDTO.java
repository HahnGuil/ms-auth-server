package br.com.hahn.auth.application.dto.request;

public record ChangePasswordRequestDTO(String email, String oldPassword, String newPassword) {
}
