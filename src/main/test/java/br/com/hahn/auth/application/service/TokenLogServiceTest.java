package br.com.hahn.auth.application.service;

import java.time.ZoneId;
import br.com.hahn.auth.application.execption.InvalidTokenException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.model.Application;
import br.com.hahn.auth.domain.model.TokenLog;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.TokenLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenLogServiceTest {
    @Mock TokenLogRepository repository;
    @Mock LoggedNowService loggedNowService;
    @Mock InvalidatedTokenService invalidatedTokenService;
    @InjectMocks TokenLogService service;

    @Test
    void savesUserTokenAndCreatesApplicationSession() {
        var user = new User(); user.setUserId(UUID.randomUUID());
        var app = new Application(); app.setId(3L); app.setPublicId(UUID.randomUUID());
        var created = LocalDateTime.now(ZoneId.systemDefault());
        var stored = new TokenLog(); stored.setIdTokenLog(UUID.randomUUID());
        when(repository.save(any(TokenLog.class))).thenReturn(stored);
        assertSame(stored, service.saveTokenLog(user, app, ScopeToken.LOGIN_TOKEN, created));
        ArgumentCaptor<TokenLog> captor = ArgumentCaptor.forClass(TokenLog.class);
        verify(repository).save(captor.capture());
        assertEquals(user.getUserId(), captor.getValue().getUserId());
        assertEquals(3L, captor.getValue().getApplicationId());
        assertEquals(ScopeToken.LOGIN_TOKEN, captor.getValue().getScopeToken());
        verify(loggedNowService).save(user.getUserId(), app.getPublicId(), stored.getIdTokenLog(), created);
    }

    @Test
    void recoverTokenDoesNotCreateActiveSession() {
        var user = new User(); user.setUserId(UUID.randomUUID());
        when(repository.save(any(TokenLog.class))).thenReturn(new TokenLog());
        service.saveTokenLog(user, null, ScopeToken.RECOVER_CODE, LocalDateTime.now(ZoneId.systemDefault()));
        verify(loggedNowService, never()).save(any(), any(), any(), any());
    }

    @Test
    void savesApplicationTokenWithoutUser() {
        var app = new Application(); app.setId(5L); app.setNameApplication("service");
        when(repository.save(any(TokenLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.saveApplicationTokenLog(app, ScopeToken.APPLICATION_TOKEN, LocalDateTime.now(ZoneId.systemDefault()));
        assertNull(result.getUserId());
        assertEquals(5L, result.getApplicationId());
        verifyNoInteractions(loggedNowService);
    }

    @Test
    void delegatesTokenValidityAndExpiredTokenLookups() {
        UUID tokenId = UUID.randomUUID();
        when(repository.findActiveTokenByLoginLogId(tokenId)).thenReturn(true);
        assertTrue(service.isTokenValid(tokenId));
        var cutoff = LocalDateTime.now(ZoneId.systemDefault());
        service.findExpiredActiveTokens(cutoff);
        verify(repository).findExpiredActiveTokens(cutoff);
    }

    @Test
    void findsTokenOrThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        var log = new TokenLog(); log.setIdTokenLog(id);
        when(repository.findById(id)).thenReturn(Optional.of(log));
        assertSame(log, service.findById(id));
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(InvalidTokenException.class, () -> service.findById(id));
    }

    @Test
    void deactivatesAndInvalidatesTokenWithinApplication() {
        UUID userId = UUID.randomUUID(), appId = UUID.randomUUID();
        var log = new TokenLog(); log.setIdTokenLog(UUID.randomUUID()); log.setUserId(userId);
        when(repository.findTopByUserIdAndApplicationPublicIdOrderByCreateDateDesc(userId, appId)).thenReturn(log);
        service.deactivateActiveUserToken(userId, appId, TypeInvalidation.LOG_OFF);
        verify(repository).deactivateActiveTokenByUserIdAndApplicationPublicId(userId, appId);
        verify(invalidatedTokenService).save(argThat(token -> userId.equals(token.getUserId()) && appId.equals(token.getApplicationPublicId()) && log.getIdTokenLog().equals(token.getLoginLogId())));
    }

    @Test
    void validatesExpectedUserScopesAndRejectsApplicationScope() {
        TokenLog login = new TokenLog(); login.setScopeToken(ScopeToken.LOGIN_TOKEN);
        assertDoesNotThrow(() -> service.isExpectedScopeToken(login));
        TokenLog invalid = new TokenLog(); invalid.setScopeToken(ScopeToken.APPLICATION_TOKEN);
        assertThrows(InvalidTokenException.class, () -> service.isExpectedScopeToken(invalid));
    }

    @Test
    void rejectsInactiveTokenLogAndDelegatesExistenceCheck() {
        TokenLog inactive = new TokenLog(); inactive.setActiveToken(false);
        assertThrows(InvalidTokenException.class, () -> service.isTokenLogValid(inactive));
        UUID tokenId = UUID.randomUUID(); when(repository.existsById(tokenId)).thenReturn(true);
        assertTrue(service.existsById(tokenId));
    }

    @Test
    void deactivatesLegacyUserAndApplicationTokenScopes() {
        UUID userId = UUID.randomUUID(), appId = UUID.randomUUID();
        TokenLog userLog = new TokenLog(); userLog.setIdTokenLog(UUID.randomUUID()); userLog.setUserId(userId);
        when(repository.findTopByUserIdAndApplicationIdIsNullOrderByCreateDateDesc(userId)).thenReturn(userLog);
        service.deactivateActiveUserToken(userId, null, TypeInvalidation.LOG_OFF);
        verify(repository).deactivateActiveTokenByUserIdWithoutApplication(userId);
        verify(invalidatedTokenService).save(argThat(t -> userId.equals(t.getUserId()) && t.getApplicationPublicId() == null));

        Application app = new Application(); app.setPublicId(appId);
        TokenLog applicationLog = new TokenLog(); applicationLog.setIdTokenLog(UUID.randomUUID()); applicationLog.setApplication(app);
        when(repository.findTopByApplicationPublicIdAndUserIdIsNullOrderByCreateDateDesc(appId)).thenReturn(applicationLog);
        service.deactiveActiveApplicationToken(appId, TypeInvalidation.APPLICATION_REFRESH);
        verify(repository).deactivateActiveTokenByApplicationPublicId(appId);
        verify(invalidatedTokenService).save(argThat(t -> t.getUserId() == null && appId.equals(t.getApplicationPublicId())));
    }

}
