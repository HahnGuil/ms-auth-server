package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.InvalidRecoverCodeException;
import br.com.hahn.auth.application.execption.NotFoundResetPasswordRequestForUser;
import br.com.hahn.auth.application.execption.UserCanNotChangePasswordException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    private final String newPassword = "#Password25";
    private final String correctEmailFormat = "user@example.com";
    private final String recoveryCode = "123456";
    private final String encodedCode = "encodedCode";

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ResetPasswordRepository resetPasswordRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private TokenService tokenService;

    @Mock
    private TokenLogService tokenLogService;

    @InjectMocks
    private PasswordService passwordService;

    private User createUser(UUID userId, TypeUser typeUser) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail("user@example.com");
        String firstName = "John";
        user.setFirstName(firstName);
        String lastName = "Doe";
        user.setLastName(lastName);
        user.setTypeUser(typeUser);
        String encodedPassword = "encodedPassword";
        user.setPassword(encodedPassword);
        return user;
    }

    private ChangePasswordRequest createChangePasswordRequest() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setEmail("user@example.com");
        request.setOldPassword("#Password24");
        request.setNewPassword("#Password25");
        return request;
    }

    private PasswordResetRequest createPasswordResetRequest() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("user@example.com");
        return request;
    }

    private ValidateCodeRequest createValidateCodeRequest() {
        ValidateCodeRequest request = new ValidateCodeRequest();
        request.setEmail("user@example.com");
        request.setRecoveryCode("123456");
        return request;
    }

    private ResetPassword createResetPassword(LocalDateTime expirationDate) {
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setUserEmail("user@example.com");
        resetPassword.setRecoverCode(encodedCode);
        resetPassword.setExpirationDate(expirationDate);
        return resetPassword;
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        UUID userId = UUID.randomUUID();
        ChangePasswordRequest request = createChangePasswordRequest();
        User user = createUser(userId, TypeUser.DIRECT_USER);
        String encodedNewPassword = "encodedNewPassword";

        when(userService.findByEmail(correctEmailFormat)).thenReturn(user);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        String oldPassword = "#Password24";
        doNothing().when(authService).validateOldPassword(user, oldPassword);
        doNothing().when(userService).updatePassword(eq(correctEmailFormat), eq(userId), anyString(), any(LocalDateTime.class));
        doNothing().when(authService).doLogOff(userId, TypeInvalidation.CHANGE_PASSWORD);

        passwordService.changePassword(request);

        verify(userService).findByEmail(correctEmailFormat);
        verify(authService).validateOldPassword(user, oldPassword);
        verify(passwordEncoder).encode(newPassword);
        verify(userService).updatePassword(eq(correctEmailFormat), eq(userId), eq(encodedNewPassword), any(LocalDateTime.class));
        verify(authService).doLogOff(userId, TypeInvalidation.CHANGE_PASSWORD);
    }

    @Test
    void shouldThrowExceptionWhenOAuthUserTriesToChangePassword() {
        UUID userId = UUID.randomUUID();
        ChangePasswordRequest request = createChangePasswordRequest();
        User user = createUser(userId, TypeUser.OAUTH_USER);

        when(userService.findByEmail(correctEmailFormat)).thenReturn(user);

        assertThrows(UserCanNotChangePasswordException.class, () -> passwordService.changePassword(request));
        verify(userService).findByEmail(correctEmailFormat);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(authService);
    }

    @Test
    void shouldRequestValidateCodeSuccessfully() {
        UUID userId = UUID.randomUUID();
        PasswordResetRequest request = createPasswordResetRequest();
        User user = createUser(userId, TypeUser.DIRECT_USER);

        when(userService.findByEmail(correctEmailFormat)).thenReturn(user);
        when(resetPasswordRepository.existsByUserEmail(correctEmailFormat)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(encodedCode);
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        SuccessResponse response = passwordService.requestValidateCode(request);

        assertNotNull(response);
        assertNotNull(response.getMessage());
        verify(userService).findByEmail(correctEmailFormat);
        verify(resetPasswordRepository).save(any(ResetPassword.class));
        verify(emailService).sendEmail(eq(correctEmailFormat), anyString(), anyString());
    }

    @Test
    void shouldDeleteExistingResetPasswordBeforeCreatingNew() {
        UUID userId = UUID.randomUUID();
        Long resetPasswordId = 1L;
        PasswordResetRequest request = createPasswordResetRequest();
        User user = createUser(userId, TypeUser.DIRECT_USER);

        ResetPassword existingResetPassword = new ResetPassword();
        existingResetPassword.setId(resetPasswordId);
        existingResetPassword.setUserEmail(correctEmailFormat);

        when(userService.findByEmail(correctEmailFormat)).thenReturn(user);
        when(resetPasswordRepository.existsByUserEmail(correctEmailFormat)).thenReturn(true);
        when(resetPasswordRepository.findByUserEmail(correctEmailFormat)).thenReturn(Optional.of(existingResetPassword));
        when(passwordEncoder.encode(anyString())).thenReturn(encodedCode);
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        passwordService.requestValidateCode(request);

        verify(resetPasswordRepository).deleteById(resetPasswordId);
        verify(resetPasswordRepository).save(any(ResetPassword.class));
    }

    @Test
    void shouldValidateResetCodeSuccessfully() {
        UUID userId = UUID.randomUUID();
        ValidateCodeRequest request = createValidateCodeRequest();
        User user = createUser(userId, TypeUser.DIRECT_USER);
        ResetPassword resetPassword = createResetPassword(LocalDateTime.now().plusMinutes(10));
        TokenLog tokenLog = new TokenLog();

        when(resetPasswordRepository.findByUserEmail(correctEmailFormat)).thenReturn(Optional.of(resetPassword));
        when(passwordEncoder.matches(recoveryCode, encodedCode)).thenReturn(true);
        when(userService.findByEmail(correctEmailFormat)).thenReturn(user);
        when(tokenLogService.saveTokenLog(eq(user), eq(ScopeToken.RECOVER_CODE), any(LocalDateTime.class))).thenReturn(tokenLog);
        String recoverToken = "recoverToken123";
        when(tokenService.generateRecoverToken(resetPassword, tokenLog)).thenReturn(recoverToken);

        ValidateCodeResponse response = passwordService.validateResetCode(request);

        assertNotNull(response);
        assertEquals(recoverToken, response.getRecoverToken());
        verify(passwordEncoder).matches(recoveryCode, encodedCode);
    }

    @Test
    void shouldThrowExceptionWhenRecoveryCodeDoesNotMatch() {
        ValidateCodeRequest request = createValidateCodeRequest();
        ResetPassword resetPassword = createResetPassword(LocalDateTime.now().plusMinutes(10));

        when(resetPasswordRepository.findByUserEmail(correctEmailFormat)).thenReturn(Optional.of(resetPassword));
        when(passwordEncoder.matches(recoveryCode, encodedCode)).thenReturn(false);

        assertThrows(InvalidRecoverCodeException.class, () -> passwordService.validateResetCode(request));
    }

    @Test
    void shouldThrowExceptionWhenRecoveryCodeIsExpired() {
        ValidateCodeRequest request = createValidateCodeRequest();
        ResetPassword resetPassword = createResetPassword(LocalDateTime.now().minusMinutes(1));

        when(resetPasswordRepository.findByUserEmail(correctEmailFormat)).thenReturn(Optional.of(resetPassword));
        when(passwordEncoder.matches(recoveryCode, encodedCode)).thenReturn(true);

        assertThrows(InvalidRecoverCodeException.class, () -> passwordService.validateResetCode(request));
    }

    @Test
    void shouldThrowExceptionWhenResetPasswordNotFoundForEmail() {
        ValidateCodeRequest request = createValidateCodeRequest();

        when(resetPasswordRepository.findByUserEmail(correctEmailFormat)).thenReturn(Optional.empty());

        assertThrows(NotFoundResetPasswordRequestForUser.class, () -> passwordService.validateResetCode(request));
    }

    @Test
    void shouldResetUserPasswordSuccessfully() {
        UUID userId = UUID.randomUUID();
        String encodedNewPassword = "encodedNewPassword";

        NewPasswordRequest request = new NewPasswordRequest();
        request.setNewPassword(newPassword);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(correctEmailFormat);
        when(jwt.<String>getClaim("scope")).thenReturn(ScopeToken.RECOVER_CODE.getValue());
        when(jwt.<String>getClaim("user_id")).thenReturn(userId.toString());

        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        doNothing().when(userService).updatePassword(eq(correctEmailFormat), eq(userId), eq(encodedNewPassword), any(LocalDateTime.class));
        doNothing().when(tokenLogService).deactivateActiveToken(userId, TypeInvalidation.RESET_PASSWORD);

        passwordService.resetUserPassword(jwt, request);

        verify(passwordEncoder).encode(newPassword);
        verify(userService).updatePassword(eq(correctEmailFormat), eq(userId), eq(encodedNewPassword), any(LocalDateTime.class));
        verify(tokenLogService).deactivateActiveToken(userId, TypeInvalidation.RESET_PASSWORD);
    }

    @Test
    void shouldDeleteExpiredResetPasswordEntries() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(1);

        when(resetPasswordRepository.deleteByExpirationDateBefore(cutoffDate)).thenReturn(5);

        int deletedCount = passwordService.deleteByExpirationDateBefore(cutoffDate);

        assertEquals(5, deletedCount);
        verify(resetPasswordRepository).deleteByExpirationDateBefore(cutoffDate);
    }

    @Test
    void shouldValidateTokenForChangePasswordSuccessfully() {
        UUID tokenLogId = UUID.randomUUID();

        Jwt jwt = mock(Jwt.class);
        when(jwt.<String>getClaim("token_log_id")).thenReturn(tokenLogId.toString());

        TokenLog tokenLog = new TokenLog();
        tokenLog.setIdTokenLog(tokenLogId);
        tokenLog.setScopeToken(ScopeToken.LOGIN_TOKEN);
        tokenLog.setActiveToken(true);

        when(tokenLogService.findById(tokenLogId)).thenReturn(tokenLog);
        doNothing().when(tokenLogService).isExpectedScopeToken(tokenLog);
        doNothing().when(tokenLogService).isTokenLogValid(tokenLog);

        passwordService.validateTokenForChangePassword(jwt);

        verify(tokenLogService).findById(tokenLogId);
        verify(tokenLogService).isExpectedScopeToken(tokenLog);
        verify(tokenLogService).isTokenLogValid(tokenLog);
    }

    @Test
    void shouldSaveResetPasswordWithCorrectExpirationTime() {
        UUID userId = UUID.randomUUID();
        PasswordResetRequest request = createPasswordResetRequest();
        User user = createUser(userId, TypeUser.DIRECT_USER);

        when(userService.findByEmail(correctEmailFormat)).thenReturn(user);
        when(resetPasswordRepository.existsByUserEmail(correctEmailFormat)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(encodedCode);
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        passwordService.requestValidateCode(request);

        ArgumentCaptor<ResetPassword> captor = ArgumentCaptor.forClass(ResetPassword.class);
        verify(resetPasswordRepository).save(captor.capture());

        ResetPassword savedResetPassword = captor.getValue();
        assertNotNull(savedResetPassword.getExpirationDate());
        assertTrue(savedResetPassword.getExpirationDate().isAfter(LocalDateTime.now().plusMinutes(29)));
        assertTrue(savedResetPassword.getExpirationDate().isBefore(LocalDateTime.now().plusMinutes(31)));
    }
}
