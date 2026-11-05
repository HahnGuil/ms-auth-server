package br.com.hahn.auth.infrastructure.security;

import java.time.ZoneId;
import br.com.hahn.auth.application.execption.InvalidCredentialsException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.TokenLog;
import br.com.hahn.auth.domain.model.Application;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.enums.TypeUser;
import br.com.hahn.auth.domain.enums.UserApplicationRole;
import br.com.hahn.auth.application.service.TokenLogService;
import com.auth0.jwt.JWT;
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

    @Mock
    private TokenLogService tokenLogService;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void shouldGenerateRecoverTokenSuccessfully() {
        ResetPassword resetPassword = new ResetPassword();
        TokenLog tokenLog = new TokenLog();
        tokenLog.setUserId(UUID.randomUUID());
        tokenLog.setScopeToken(ScopeToken.RECOVER_CODE);
        tokenLog.setIdTokenLog(UUID.randomUUID());
        tokenLog.setCreateDate(LocalDateTime.now(ZoneId.systemDefault()));

        when(keyManager.getCurrentKeyPair()).thenReturn(generateKeyPair());
        when(keyManager.getCurrentKeyId()).thenReturn("key-id");

        String token = tokenService.generateRecoverToken(resetPassword, tokenLog, UUID.randomUUID());

        assertNotNull(token);
    }

    @Test
    void shouldThrowExceptionWhenPrivateKeyUnavailableForRecoverToken() {
        ResetPassword resetPassword = new ResetPassword();
        TokenLog tokenLog = new TokenLog();
        tokenLog.setUserId(UUID.randomUUID());
        tokenLog.setScopeToken(ScopeToken.RECOVER_CODE);
        tokenLog.setIdTokenLog(UUID.randomUUID());
        tokenLog.setCreateDate(LocalDateTime.now(ZoneId.systemDefault()));

        assertThrows(IllegalStateException.class, () -> tokenService.generateRecoverToken(resetPassword, tokenLog, UUID.randomUUID()));
    }

    @Test
    void shouldThrowInvalidCredentialsExceptionForInvalidToken() {
        String token = "invalid.jwt.token";

        assertThrows(InvalidCredentialsException.class, () -> tokenService.validateToken(token));
    }


    @Test
    void userTokensExposeOnlyTheirApplicationAndCanBeValidated() {
        KeyPair keys = generateKeyPair();
        when(keyManager.getCurrentKeyPair()).thenReturn(keys);
        when(keyManager.getCurrentKeyId()).thenReturn("key-id");
        when(keyManager.getPublicKeys()).thenReturn(java.util.Map.of("key-id", keys.getPublic()));
        User user = new User(); user.setUserId(UUID.randomUUID()); user.setEmail("user@example.com");
        user.setTypeUser(TypeUser.DIRECT_USER); user.setUserApplicationRole(UserApplicationRole.USER);
        Application app = new Application(); app.setId(8L); app.setPublicId(UUID.randomUUID()); app.setNameApplication("private-app");
        TokenLog tokenLog = new TokenLog(); tokenLog.setIdTokenLog(UUID.randomUUID()); tokenLog.setCreateDate(LocalDateTime.now(ZoneId.systemDefault())); tokenLog.setScopeToken(ScopeToken.LOGIN_TOKEN);
        String token = tokenService.generateUserToken(user, tokenLog, app);
        assertEquals(user.getEmail(), JWT.decode(token).getSubject());
        assertEquals(app.getPublicId().toString(), JWT.decode(token).getClaim("application_public_id").asString());
        assertEquals(java.util.List.of("private-app"), JWT.decode(token).getClaim("applications").asList(String.class));
        when(tokenLogService.existsById(tokenLog.getIdTokenLog())).thenReturn(true);
        when(tokenLogService.findById(tokenLog.getIdTokenLog())).thenReturn(tokenLog);
        assertEquals(user.getEmail(), tokenService.validateToken(token));
    }

    @Test
    void applicationTokensAndRefreshTokensCarryApplicationIdentity() {
        KeyPair keys = generateKeyPair();
        when(keyManager.getCurrentKeyPair()).thenReturn(keys);
        when(keyManager.getCurrentKeyId()).thenReturn("key-id");
        Application app = new Application(); app.setId(2L); app.setPublicId(UUID.randomUUID()); app.setNameApplication("app");
        TokenLog log = new TokenLog(); log.setIdTokenLog(UUID.randomUUID()); log.setCreateDate(LocalDateTime.now(ZoneId.systemDefault())); log.setScopeToken(ScopeToken.APPLICATION_TOKEN);
        String token = tokenService.generateApplicationToken(app, log);
        assertEquals(app.getPublicId().toString(), JWT.decode(token).getSubject());
        assertEquals("app", JWT.decode(token).getClaim("application_name").asString());
        TokenLog refreshLog = new TokenLog(); refreshLog.setIdTokenLog(UUID.randomUUID()); refreshLog.setCreateDate(LocalDateTime.now(ZoneId.systemDefault())); refreshLog.setApplicationId(app.getId());
        String refresh = tokenService.generateApplicationRefreshToken(app, refreshLog);
        assertEquals(ScopeToken.APPLICATION_REFRESH_TOKEN.getValue(), JWT.decode(refresh).getClaim("scope").asString());
        assertEquals(app.getId().toString(), JWT.decode(refresh).getClaim("application_id").asString());
    }


    @Test
    void generatesUserRefreshAndRecoveryTokensWithApplicationClaims() {
        when(keyManager.getCurrentKeyPair()).thenReturn(generateKeyPair());
        when(keyManager.getCurrentKeyId()).thenReturn("key-id");
        User user = new User(); user.setUserId(UUID.randomUUID()); user.setEmail("user@example.com");
        Application app = new Application(); app.setId(6L); app.setPublicId(UUID.randomUUID());
        TokenLog refreshLog = new TokenLog(); refreshLog.setIdTokenLog(UUID.randomUUID()); refreshLog.setCreateDate(LocalDateTime.now(ZoneId.systemDefault()));
        String refreshToken = tokenService.generateRefreshToken(user, refreshLog, app);
        assertEquals(ScopeToken.REFRESH_TOKEN.getValue(), JWT.decode(refreshToken).getClaim("scope").asString());
        assertEquals(app.getPublicId().toString(), JWT.decode(refreshToken).getClaim("application_public_id").asString());
        ResetPassword reset = new ResetPassword(); reset.setUserEmail(user.getEmail());
        TokenLog recoverLog = new TokenLog(); recoverLog.setUserId(user.getUserId()); recoverLog.setIdTokenLog(UUID.randomUUID()); recoverLog.setCreateDate(LocalDateTime.now(ZoneId.systemDefault())); recoverLog.setScopeToken(ScopeToken.RECOVER_CODE);
        String recovery = tokenService.generateRecoverToken(reset, recoverLog, app.getPublicId());
        assertEquals(app.getPublicId().toString(), JWT.decode(recovery).getClaim("application_public_id").asString());
    }

    @Test
    void rejectsExpiredAccessTokenAndUnknownTokenLog() {
        KeyPair keys = generateKeyPair();
        when(keyManager.getCurrentKeyPair()).thenReturn(keys);
        when(keyManager.getCurrentKeyId()).thenReturn("key-id");
        when(keyManager.getPublicKeys()).thenReturn(java.util.Map.of("key-id", keys.getPublic()));
        User user = new User(); user.setUserId(UUID.randomUUID()); user.setEmail("user@example.com"); user.setTypeUser(TypeUser.DIRECT_USER); user.setUserApplicationRole(UserApplicationRole.USER);
        TokenLog expired = new TokenLog(); expired.setIdTokenLog(UUID.randomUUID()); expired.setCreateDate(LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(20)); expired.setScopeToken(ScopeToken.LOGIN_TOKEN);
        String token = tokenService.generateUserToken(user, expired);
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