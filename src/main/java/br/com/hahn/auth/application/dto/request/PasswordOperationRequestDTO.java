package br.com.hahn.auth.application.dto.request;

public record PasswordOperationRequestDTO(String email, String oldPassword, String newPassword, String recoverCode) {
    public static PasswordOperationRequestDTO forChangePassword(String email, String oldPassword, String newPassword) {
        return new PasswordOperationRequestDTO(email, oldPassword, newPassword, null);
    }

    public static PasswordOperationRequestDTO forForgotPassword(String email) {
        return new PasswordOperationRequestDTO(email, null, null, null);
    }

    public static PasswordOperationRequestDTO forResetPassword(String email, String recoverCode) {
        return new PasswordOperationRequestDTO(email, null, null, recoverCode);
    }
}
