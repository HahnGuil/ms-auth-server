package br.com.hahn.auth.infrastructure.scheduling;

import br.com.hahn.auth.application.service.PasswordService;
import br.com.hahn.auth.application.service.TokenLogService;
import br.com.hahn.auth.application.service.UserService;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.enums.UserRole;
import br.com.hahn.auth.domain.model.Application;
import br.com.hahn.auth.domain.model.PasswordParameters;
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationSchedulerTest {
    @Mock EmailService emailService;
    @Mock UserService userService;
    @Mock TokenLogService tokenLogService;
    @Mock PasswordService passwordService;
    @InjectMocks ApplicationScheduler scheduler;

    private PasswordParameters password(boolean normalRole, String apiKey) {
        User user = new User(); user.setEmail("test@example.com"); user.setFirstName("T"); user.setRole(normalRole ? UserRole.USER_NORMAL : UserRole.USER_ADMIN);
        Application app = new Application(); app.setNameApplication("Test app"); app.setApplicationEmail("security@test"); app.setResendApiKey(apiKey); app.setPublicId(UUID.randomUUID());
        PasswordParameters parameters = new PasswordParameters(); parameters.setUser(user); parameters.setApplication(app); return parameters;
    }

    @Test
    void cleansExpiredRecoveryCodes() {
        when(passwordService.deleteByExpirationDateBefore(any(LocalDateTime.class))).thenReturn(5);
        scheduler.cleanExpiredResetRecoverCodes();
        verify(passwordService).deleteByExpirationDateBefore(any(LocalDateTime.class));
    }

    @Test
    void checksAllWarningDaysAndSendsUsingApplicationSettings() {
        when(userService.getUsersWithPasswordExpiringInDays(anyInt())).thenReturn(List.of(password(true, "resend-key")));
        when(emailService.sendEmail(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        scheduler.alertExpiredUser();
        var captor = org.mockito.ArgumentCaptor.forClass(Integer.class);
        verify(userService, times(4)).getUsersWithPasswordExpiringInDays(captor.capture());
        assertArrayEquals(new Integer[]{20, 15, 10, 5}, captor.getAllValues().toArray(Integer[]::new));
        verify(emailService, times(4)).sendEmail(eq("resend-key"), eq("security@test"), eq("test@example.com"), eq("Your password will expire soon"), contains("Test app"));
    }

    @Test
    void skipsWarningsWithoutResendKeyAndForNonNormalUsers() {
        when(userService.getUsersWithPasswordExpiringInDays(anyInt())).thenReturn(List.of(password(true, null), password(false, "resend-key")));
        scheduler.alertExpiredUser();
        verifyNoInteractions(emailService);
    }

    @Test
    void invokesPasswordBlocking() {
        scheduler.blockUser();
        verify(userService).findUserToBlock();
    }

    @Test
    void invalidatesExpiredUserTokenInItsApplication() {
        UUID userId = UUID.randomUUID(), appId = UUID.randomUUID();
        Application app = new Application(); app.setPublicId(appId);
        TokenLog tokenLog = new TokenLog(); tokenLog.setUserId(userId); tokenLog.setApplication(app);
        when(tokenLogService.findExpiredActiveTokens(any(LocalDateTime.class))).thenReturn(List.of(tokenLog));
        scheduler.invalidTokenScheduler();
        verify(tokenLogService).deactivateActiveUserToken(userId, appId, TypeInvalidation.EXPIRATION_TIME);
    }
}
