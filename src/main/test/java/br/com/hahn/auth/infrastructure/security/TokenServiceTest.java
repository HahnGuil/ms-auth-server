package br.com.hahn.auth.infrastructure.security;

import br.com.hahn.auth.application.execption.InvalidCredentialsException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.TokenLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private KeyManager keyManager;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void shouldGenerateRecoverTokenSuccessfully() {
        ResetPassword resetPassword = new ResetPassword();
        TokenLog tokenLog = new TokenLog();
        tokenLog.setUserId(UUID.randomUUID());
        tokenLog.setScopeToken(ScopeToken.RECOVER_CODE);
        tokenLog.setIdTokenLog(UUID.randomUUID());
        tokenLog.setCreateDate(LocalDateTime.now());

        when(keyManager.getCurrentKeyPair()).thenReturn(generateKeyPair());
        when(keyManager.getCurrentKeyId()).thenReturn("key-id");

        String token = tokenService.generateRecoverToken(resetPassword, tokenLog);

        assertNotNull(token);
    }

    @Test
    void shouldThrowExceptionWhenPrivateKeyUnavailableForRecoverToken() {
        ResetPassword resetPassword = new ResetPassword();
        TokenLog tokenLog = new TokenLog();
        tokenLog.setUserId(UUID.randomUUID());
        tokenLog.setScopeToken(ScopeToken.RECOVER_CODE);
        tokenLog.setIdTokenLog(UUID.randomUUID());
        tokenLog.setCreateDate(LocalDateTime.now());

        assertThrows(IllegalStateException.class, () -> tokenService.generateRecoverToken(resetPassword, tokenLog));
    }

    @Test
    void shouldThrowInvalidCredentialsExceptionForInvalidToken() {
        String token = "invalid.jwt.token";

        assertThrows(InvalidCredentialsException.class, () -> tokenService.validateToken(token));
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}