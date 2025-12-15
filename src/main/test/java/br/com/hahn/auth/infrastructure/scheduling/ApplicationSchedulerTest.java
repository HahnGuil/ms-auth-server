package br.com.hahn.auth.infrastructure.scheduling;

import br.com.hahn.auth.application.service.PasswordService;
import br.com.hahn.auth.application.service.TokenLogService;
import br.com.hahn.auth.application.service.UserService;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.enums.UserRole;
import br.com.hahn.auth.domain.model.TokenLog;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.infrastructure.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationSchedulerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @Mock
    private TokenLogService tokenLogService;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private ApplicationScheduler applicationScheduler;

    @Test
    void shouldCleanExpiredResetRecoverCodesSuccessfully() {
        when(passwordService.deleteByExpirationDateBefore(any(LocalDateTime.class))).thenReturn(5);

        applicationScheduler.cleanExpiredResetRecoverCodes();

        verify(passwordService).deleteByExpirationDateBefore(any(LocalDateTime.class));
        verifyNoMoreInteractions(passwordService);
    }

    @Test
    void shouldSendNotificationEmailsToUsersWithExpiringPasswords() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setRole(UserRole.USER_NORMAL);
        when(userService.getUsersWithPasswordExpiringInDays(anyInt())).thenReturn(List.of(user));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        applicationScheduler.alertExpiredUser();

        verify(userService, times(4)).getUsersWithPasswordExpiringInDays(anyInt());
        verify(emailService, times(4)).sendEmail(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    void shouldNotSendEmailsToUsersWithNonNormalRoles() {
        User user = new User();
        user.setRole(UserRole.USER_ADMIN);
        when(userService.getUsersWithPasswordExpiringInDays(anyInt())).thenReturn(List.of(user));

        applicationScheduler.alertExpiredUser();

        verify(userService, times(4)).getUsersWithPasswordExpiringInDays(anyInt());
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldBlockUsersBasedOnCriteria() {
        applicationScheduler.blockUser();

        verify(userService).findUserToBlock();
        verifyNoMoreInteractions(userService);
    }

    @Test
    void shouldInvalidateExpiredTokensSuccessfully() {
        TokenLog tokenLog = new TokenLog();
        UUID userId = UUID.randomUUID();
        tokenLog.setUserId(userId);
        when(tokenLogService.findExpiredActiveTokens(any(LocalDateTime.class))).thenReturn(List.of(tokenLog));

        applicationScheduler.invalidTokenScheduler();

        verify(tokenLogService).findExpiredActiveTokens(any(LocalDateTime.class));
        verify(tokenLogService).deactivateActiveToken(userId, TypeInvalidation.EXPIRATION_TIME);
    }
}