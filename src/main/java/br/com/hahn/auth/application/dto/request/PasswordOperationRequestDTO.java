package br.com.hahn.auth.application.dto.request;

public record PasswordOperationRequestDTO(String email, String oldPassword, String newPassword, String recoverCode) {

}
