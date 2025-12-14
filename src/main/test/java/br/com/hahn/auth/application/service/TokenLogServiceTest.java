package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.InvalidTokenException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.model.TokenLog;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.TokenLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class TokenLogServiceTest {

    @Mock
    private TokenLogRepository tokenLogRepository;

    @Mock
    private LoggedNowService loggedNowService;

    @Mock
    private InvalidatedTokenService invalidatedTokenService;

    @InjectMocks
    private TokenLogService tokenLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("deactivateActiveToken")
    class DeactivateActiveToken {

        @Test
        @DisplayName("Should deactivate active token and invalidate it successfully")
        void deactivateActiveTokenSuccessfully() {
            UUID userId = UUID.randomUUID();
            TokenLog tokenLog = new TokenLog();
            tokenLog.setIdTokenLog(UUID.randomUUID());
            tokenLog.setUserId(userId);

            when(tokenLogRepository.findTopByUserIdOrderByCreateDateDesc(userId)).thenReturn(tokenLog);

            tokenLogService.deactivateActiveToken(userId, TypeInvalidation.EXPIRATION_TIME);

            verify(tokenLogRepository).deactivateActiveTokenByUserId(userId);
            verify(invalidatedTokenService).save(any());
        }

        @Test
        @DisplayName("Should throw exception when no token log is found for user")
        void deactivateActiveTokenThrowsExceptionWhenNoTokenLogFound() {
            UUID userId = UUID.randomUUID();

            when(tokenLogRepository.findTopByUserIdOrderByCreateDateDesc(userId)).thenReturn(null);

            assertThrows(NullPointerException.class, () -> tokenLogService.deactivateActiveToken(userId, TypeInvalidation.EXPIRATION_TIME));
        }
    }

    @Nested
    @DisplayName("saveTokenLog")
    class SaveTokenLog {

        @Test
        @DisplayName("Should save token log and call LoggedNowService for non-recover scope")
        void saveTokenLogWithNonRecoverScope() {
            User user = new User();
            user.setUserId(UUID.randomUUID());
            LocalDateTime createDate = LocalDateTime.now();
            ScopeToken scopeToken = ScopeToken.LOGIN_TOKEN;

            TokenLog tokenLog = new TokenLog();
            tokenLog.setIdTokenLog(UUID.randomUUID());

            when(tokenLogRepository.save(any())).thenReturn(tokenLog);

            TokenLog result = tokenLogService.saveTokenLog(user, scopeToken, createDate);

            assertNotNull(result);
            verify(tokenLogRepository).save(any());
            verify(loggedNowService).save(user.getUserId(), tokenLog.getIdTokenLog(), createDate);
        }

        @Test
        @DisplayName("Should save token log without calling LoggedNowService for recover scope")
        void saveTokenLogWithRecoverScope() {
            User user = new User();
            user.setUserId(UUID.randomUUID());
            LocalDateTime createDate = LocalDateTime.now();
            ScopeToken scopeToken = ScopeToken.RECOVER_CODE;

            TokenLog tokenLog = new TokenLog();
            tokenLog.setIdTokenLog(UUID.randomUUID());

            when(tokenLogRepository.save(any())).thenReturn(tokenLog);

            TokenLog result = tokenLogService.saveTokenLog(user, scopeToken, createDate);

            assertNotNull(result);
            verify(tokenLogRepository).save(any());
            verify(loggedNowService, never()).save(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("Should return true for valid token")
        void isTokenValidReturnsTrue() {
            UUID tokenLogId = UUID.randomUUID();

            when(tokenLogRepository.findActiveTokenByLoginLogId(tokenLogId)).thenReturn(true);

            assertTrue(tokenLogService.isTokenValid(tokenLogId));
        }

        @Test
        @DisplayName("Should return false for invalid token")
        void isTokenValidReturnsFalse() {
            UUID tokenLogId = UUID.randomUUID();

            when(tokenLogRepository.findActiveTokenByLoginLogId(tokenLogId)).thenReturn(false);

            assertFalse(tokenLogService.isTokenValid(tokenLogId));
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Should return token log when found")
        void findByIdReturnsTokenLog() {
            UUID tokenLogId = UUID.randomUUID();
            TokenLog tokenLog = new TokenLog();
            tokenLog.setIdTokenLog(tokenLogId);

            when(tokenLogRepository.findById(tokenLogId)).thenReturn(Optional.of(tokenLog));

            TokenLog result = tokenLogService.findById(tokenLogId);

            assertNotNull(result);
            assertEquals(tokenLogId, result.getIdTokenLog());
        }

        @Test
        @DisplayName("Should throw exception when token log is not found")
        void findByIdThrowsExceptionWhenNotFound() {
            UUID tokenLogId = UUID.randomUUID();

            when(tokenLogRepository.findById(tokenLogId)).thenReturn(Optional.empty());

            assertThrows(InvalidTokenException.class, () -> tokenLogService.findById(tokenLogId));
        }
    }
}
