package br.com.hahn.auth.application.service;

import java.time.ZoneId;
import br.com.hahn.auth.application.execption.InvalidOperationException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.enums.TypeUser;
import br.com.hahn.auth.domain.model.*;
import br.com.hahn.auth.domain.respository.ResetPasswordRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import br.com.hahn.auth.infrastructure.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {
    private final UUID appPublicId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    @Mock UserService userService;
    @Mock AuthService authService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ResetPasswordRepository resetPasswordRepository;
    @Mock EmailService emailService;
    @Mock TokenService tokenService;
    @Mock TokenLogService tokenLogService;
    @Mock ApplicationService applicationService;
    @InjectMocks PasswordService passwordService;

    private User user() {
        User user = new User(); user.setUserId(UUID.randomUUID()); user.setEmail("user@example.com");
        user.setFirstName("First"); user.setLastName("Last"); user.setTypeUser(TypeUser.DIRECT_USER);
        return user;
    }
    private Application application() {
        Application app = new Application(); app.setId(9L); app.setPublicId(appPublicId);
        app.setApplicationEmail("security@app.test"); app.setResendApiKey("resend-key"); app.setNameApplication("app");
        return app;
    }

    @Test
    void recoveryCreatesApplicationScopedCodeAndSendsUsingApplicationCredentials() {
        User user = user(); Application app = application();
        PasswordParameters parameters = new PasswordParameters(); parameters.setApplication(app); parameters.setUser(user);
        var request = new PasswordRecoverCodeRequest(user.getEmail(), appPublicId);
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(userService.findPasswordParameters(user, appPublicId)).thenReturn(parameters);
        when(resetPasswordRepository.findByUserEmailAndApplicationId(user.getEmail(), app.getId())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-code");
        when(emailService.sendEmail(eq("resend-key"), eq("security@app.test"), eq(user.getEmail()), eq("Password Reset Request"), anyString())).thenReturn(Mono.empty());

        assertNotNull(passwordService.passwordRecoveryCodeRequest(request).getMessage());

        ArgumentCaptor<ResetPassword> saved = ArgumentCaptor.forClass(ResetPassword.class);
        verify(resetPasswordRepository).save(saved.capture());
        assertEquals(user.getEmail(), saved.getValue().getUserEmail());
        assertEquals(app.getId(), saved.getValue().getApplicationId());
        assertEquals("encoded-code", saved.getValue().getRecoverCode());
        verify(emailService).sendEmail(eq("resend-key"), eq("security@app.test"), eq(user.getEmail()), eq("Password Reset Request"), contains("password reset code"));
    }

    @Test
    void recoveryWithoutApplicationApiKeyFailsClearly() {
        User user = user(); Application app = application(); app.setResendApiKey(null);
        PasswordParameters parameters = new PasswordParameters(); parameters.setApplication(app);
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(userService.findPasswordParameters(user, appPublicId)).thenReturn(parameters);
        when(resetPasswordRepository.findByUserEmailAndApplicationId(user.getEmail(), app.getId())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        assertThrows(InvalidOperationException.class, () -> passwordService.passwordRecoveryCodeRequest(new PasswordRecoverCodeRequest(user.getEmail(), appPublicId)));
        verifyNoInteractions(emailService);
    }

    @Test
    void validatesRecoveryCodeWithinRequestedApplication() {
        User user = user(); Application app = application();
        ResetPassword reset = new ResetPassword(); reset.setUserEmail(user.getEmail());
        reset.setApplicationId(app.getId()); reset.setRecoverCode("hash"); reset.setExpirationDate(LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(10));
        TokenLog log = new TokenLog();
        when(applicationService.findByPublicId(appPublicId)).thenReturn(app);
        when(resetPasswordRepository.findByUserEmailAndApplicationId(user.getEmail(), app.getId())).thenReturn(Optional.of(reset));
        when(passwordEncoder.matches("123456", "hash")).thenReturn(true);
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(tokenLogService.saveTokenLog(eq(user), eq(app), eq(ScopeToken.RECOVER_CODE), any())).thenReturn(log);
        when(tokenService.generateRecoverToken(reset, log, appPublicId)).thenReturn("recover-token");

        var response = passwordService.validateResetCode(new ValidateCodeRequest(appPublicId, user.getEmail(), "123456"));
        assertEquals("recover-token", response.getRecoverToken());
        verify(resetPasswordRepository).findByUserEmailAndApplicationId(user.getEmail(), app.getId());
    }

    @Test
    void changingPasswordUpdatesOnlyRequestedApplicationAndLogsItOff() {
        User user = user(); Application app = application();
        PasswordParameters parameters = new PasswordParameters(); parameters.setUser(user); parameters.setApplication(app); parameters.setPassword("old-hash");
        var request = new ChangePasswordRequest(appPublicId, user.getEmail(), "OldPass!1", "NewPass!1");
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(userService.findPasswordParameters(user, appPublicId)).thenReturn(parameters);
        when(passwordEncoder.encode("NewPass!1")).thenReturn("new-hash");
        passwordService.changePassword(request);
        verify(authService).validateOldPassword(parameters, "OldPass!1");
        verify(userService).updatePassword(eq(parameters), eq("new-hash"), any(LocalDateTime.class));
        verify(authService).doLogOff(user.getUserId(), appPublicId, TypeInvalidation.CHANGE_PASSWORD);
    }

    @Test
    void resettingPasswordReadsApplicationFromRecoveryJwtAndInvalidatesOnlyThatToken() {
        User user = user(); PasswordParameters parameters = new PasswordParameters(); parameters.setUser(user);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(user.getEmail());
        when(jwt.getClaim("scope")).thenReturn(ScopeToken.RECOVER_CODE.getValue());
        when(jwt.getClaim("user_id")).thenReturn(user.getUserId().toString());
        when(jwt.getClaimAsString("application_public_id")).thenReturn(appPublicId.toString());
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(userService.findPasswordParameters(user, appPublicId)).thenReturn(parameters);
        when(passwordEncoder.encode("NewPass!1")).thenReturn("encoded");
        passwordService.resetUserPassword(jwt, new NewPasswordRequest("NewPass!1"));
        verify(userService).updatePassword(eq(parameters), eq("encoded"), any(LocalDateTime.class));
        verify(tokenLogService).deactivateActiveUserToken(user.getUserId(), appPublicId, TypeInvalidation.RESET_PASSWORD);
    }

    @Test
    void validatesChangePasswordTokenMatchesApplication() {
        UUID tokenId = UUID.randomUUID(); Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("token_log_id")).thenReturn(tokenId.toString());
        when(jwt.getClaimAsString("application_public_id")).thenReturn(appPublicId.toString());
        TokenLog tokenLog = new TokenLog();
        when(tokenLogService.findById(tokenId)).thenReturn(tokenLog);
        passwordService.validateTokenForChangePassword(jwt, appPublicId);
        verify(tokenLogService).isExpectedScopeToken(tokenLog);
        verify(tokenLogService).isTokenLogValid(tokenLog);
    }
}
