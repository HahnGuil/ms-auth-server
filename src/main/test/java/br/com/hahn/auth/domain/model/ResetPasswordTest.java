package br.com.hahn.auth.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ResetPasswordTest {

    @Test
    void shouldCreateResetPasswordWithAllFields() {
        Long id = 1L;
        String recoverCode = "ABC123";
        String userEmail = "user@example.com";
        LocalDateTime expirationDate = LocalDateTime.now().plusHours(1);

        ResetPassword resetPassword = new ResetPassword(id, recoverCode, userEmail, expirationDate);

        assertEquals(id, resetPassword.getId());
        assertEquals(recoverCode, resetPassword.getRecoverCode());
        assertEquals(userEmail, resetPassword.getUserEmail());
        assertEquals(expirationDate, resetPassword.getExpirationDate());
    }

    @Test
    void shouldAllowUpdatingRecoverCode() {
        ResetPassword resetPassword = new ResetPassword();
        String newRecoverCode = "XYZ789";

        resetPassword.setRecoverCode(newRecoverCode);

        assertEquals(newRecoverCode, resetPassword.getRecoverCode());
    }

    @Test
    void shouldAllowSettingExpirationDateToNull() {
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setExpirationDate(null);

        assertNull(resetPassword.getExpirationDate());
    }

    @Test
    void shouldHandleNullValuesForOptionalFields() {
        ResetPassword resetPassword = new ResetPassword(null, null, null, null);

        assertNull(resetPassword.getId());
        assertNull(resetPassword.getRecoverCode());
        assertNull(resetPassword.getUserEmail());
        assertNull(resetPassword.getExpirationDate());
    }

    @Test
    void shouldVerifyExpirationDateIsInFuture() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setExpirationDate(futureDate);

        assertTrue(resetPassword.getExpirationDate().isAfter(LocalDateTime.now()));
    }
}