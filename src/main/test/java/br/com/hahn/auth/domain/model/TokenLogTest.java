package br.com.hahn.auth.domain.model;

import br.com.hahn.auth.domain.enums.ScopeToken;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenLogTest {

    @Test
    void shouldCreateTokenLogWithAllFields_AndScopeTokenIsLogin() {
        UUID idTokenLog = UUID.randomUUID();
        ScopeToken scopeToken = ScopeToken.LOGIN_TOKEN;
        LocalDateTime createDate = LocalDateTime.now();
        boolean activeToken = true;
        UUID userId = UUID.randomUUID();

        TokenLog tokenLog = new TokenLog(idTokenLog, scopeToken, createDate, activeToken, null, userId);

        assertEquals(idTokenLog, tokenLog.getIdTokenLog());
        assertEquals(scopeToken, tokenLog.getScopeToken());
        assertEquals(createDate, tokenLog.getCreateDate());
        assertTrue(tokenLog.isActiveToken());
        assertEquals(userId, tokenLog.getUserId());
    }

    @Test
    void shouldCreateTokenLogWithAllFields_AndScopeTokenIsRegister() {
        UUID idTokenLog = UUID.randomUUID();
        ScopeToken scopeToken = ScopeToken.REGISTER_TOKEN;
        LocalDateTime createDate = LocalDateTime.now();
        boolean activeToken = true;
        UUID userId = UUID.randomUUID();

        TokenLog tokenLog = new TokenLog(idTokenLog, scopeToken, createDate, activeToken, null, userId);

        assertEquals(idTokenLog, tokenLog.getIdTokenLog());
        assertEquals(scopeToken, tokenLog.getScopeToken());
        assertEquals(createDate, tokenLog.getCreateDate());
        assertTrue(tokenLog.isActiveToken());
        assertEquals(userId, tokenLog.getUserId());
    }

    @Test
    void shouldCreateTokenLogWithAllFields_AndScopeTokenIsRecover() {
        UUID idTokenLog = UUID.randomUUID();
        ScopeToken scopeToken = ScopeToken.RECOVER_CODE;
        LocalDateTime createDate = LocalDateTime.now();
        boolean activeToken = true;
        UUID userId = UUID.randomUUID();

        TokenLog tokenLog = new TokenLog(idTokenLog, scopeToken, createDate, activeToken, null, userId);

        assertEquals(idTokenLog, tokenLog.getIdTokenLog());
        assertEquals(scopeToken, tokenLog.getScopeToken());
        assertEquals(createDate, tokenLog.getCreateDate());
        assertTrue(tokenLog.isActiveToken());
        assertEquals(userId, tokenLog.getUserId());
    }

    @Test
    void shouldCreateTokenLogWithAllFields_AndScopeTokenIsRefresh() {
        UUID idTokenLog = UUID.randomUUID();
        ScopeToken scopeToken = ScopeToken.REFRESH_TOKEN;
        LocalDateTime createDate = LocalDateTime.now();
        boolean activeToken = true;
        UUID userId = UUID.randomUUID();

        TokenLog tokenLog = new TokenLog(idTokenLog, scopeToken, createDate, activeToken, null, userId);

        assertEquals(idTokenLog, tokenLog.getIdTokenLog());
        assertEquals(scopeToken, tokenLog.getScopeToken());
        assertEquals(createDate, tokenLog.getCreateDate());
        assertTrue(tokenLog.isActiveToken());
        assertEquals(userId, tokenLog.getUserId());
    }

    @Test
    void shouldAllowUpdatingScopeToken() {
        TokenLog tokenLog = new TokenLog();
        ScopeToken newScope = ScopeToken.REFRESH_TOKEN;

        tokenLog.setScopeToken(newScope);

        assertEquals(newScope, tokenLog.getScopeToken());
    }

    @Test
    void shouldAllowSettingActiveTokenToFalse() {
        TokenLog tokenLog = new TokenLog();
        tokenLog.setActiveToken(false);

        assertFalse(tokenLog.isActiveToken());
    }

    @Test
    void shouldHandleNullValuesForOptionalFields() {
        TokenLog tokenLog = new TokenLog(null, null, null, false, null, null);

        assertNull(tokenLog.getIdTokenLog());
        assertNull(tokenLog.getScopeToken());
        assertNull(tokenLog.getCreateDate());
        assertFalse(tokenLog.isActiveToken());
        assertNull(tokenLog.getUserId());
    }

    @Test
    void shouldVerifyCreateDateIsInPast() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        TokenLog tokenLog = new TokenLog();
        tokenLog.setCreateDate(pastDate);

        assertTrue(tokenLog.getCreateDate().isBefore(LocalDateTime.now()));
    }
}
