package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.RecoveryCodeExpiradeException;
import br.com.hahn.auth.application.execption.ResetPasswordNotFoundException;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.ResetPasswordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResetPasswordServiceTest {

    @Mock
    private ResetPasswordRepository resetPasswordRepository;

    @InjectMocks
    private ResetPasswordService resetPasswordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByEmail_Success() {
        String email = "test@example.com";
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setUserEmail(email);

        when(resetPasswordRepository.findByUserEmail(email)).thenReturn(Optional.of(resetPassword));

        ResetPassword result = resetPasswordService.findByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getUserEmail());
        verify(resetPasswordRepository, times(1)).findByUserEmail(email);
    }

    @Test
    void testFindByEmail_NotFound() {
        String email = "notfound@example.com";

        when(resetPasswordRepository.findByUserEmail(email)).thenReturn(Optional.empty());

        assertThrows(ResetPasswordNotFoundException.class, () -> resetPasswordService.findByEmail(email));
        verify(resetPasswordRepository, times(1)).findByUserEmail(email);
    }

    @Test
    void testExistsByUserEmail() {
        String email = "test@example.com";

        when(resetPasswordRepository.existsByUserEmail(email)).thenReturn(true);

        assertTrue(resetPasswordService.existsByUserEmail(email));
        verify(resetPasswordRepository, times(1)).existsByUserEmail(email);
    }

    @Test
    void testValidateTokenExpiration_Expired() {
        String email = "test@example.com";
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setExpirationDate(LocalDateTime.now().minusMinutes(1));

        when(resetPasswordRepository.findByUserEmail(email)).thenReturn(Optional.of(resetPassword));

        assertThrows(RecoveryCodeExpiradeException.class, () -> resetPasswordService.validateTokenExpiration(email));
    }

    @Test
    void testValidateTokenExpiration_Valid() {
        String email = "test@example.com";
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setExpirationDate(LocalDateTime.now().plusMinutes(30));

        when(resetPasswordRepository.findByUserEmail(email)).thenReturn(Optional.of(resetPassword));

        assertDoesNotThrow(() -> resetPasswordService.validateTokenExpiration(email));
    }

    @Test
    void testCreateResetPassword() {
        User user = new User();
        user.setEmail("test@example.com");
        String recoverCode = "123456";

        resetPasswordService.createResetPassword(user, recoverCode);

        verify(resetPasswordRepository, times(1)).save(any(ResetPassword.class));
    }

    @Test
    void testDeleteResetExistingPassword() {
        String email = "test@example.com";
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setId(1L);

        when(resetPasswordRepository.findByUserEmail(email)).thenReturn(Optional.of(resetPassword));

        resetPasswordService.deleteResetExistingPassword(email);

        verify(resetPasswordRepository, times(1)).deleteById(resetPassword.getId());
    }

    @Test
    void testDeleteByExpirationDateBefore() {
        when(resetPasswordRepository.deleteByExpirationDateBefore(any(LocalDateTime.class))).thenReturn(5);
        assertEquals(5, resetPasswordService.deleteByExpirationDateBefore(LocalDateTime.now()));
    }
}
