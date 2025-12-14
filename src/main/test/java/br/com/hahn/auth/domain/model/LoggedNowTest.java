package br.com.hahn.auth.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoggedNowTest {


    @Test
    void shouldCreateLoggedNowWithAllFields() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID tokenLogId = UUID.randomUUID();
        LocalDateTime dateLogin = LocalDateTime.now();
        LocalDateTime dateRefresh = LocalDateTime.now();
        boolean isUseRefresh = true;

        LoggedNow loggedNow = new LoggedNow(id, userId, tokenLogId, dateLogin, isUseRefresh, dateRefresh);

        assertEquals(id, loggedNow.getId());
        assertEquals(userId, loggedNow.getUserId());
        assertEquals(tokenLogId, loggedNow.getTokenLogId());
        assertEquals(dateLogin, loggedNow.getDateLogin());
        assertTrue(loggedNow.isUseRefresh());
        assertEquals(dateRefresh, loggedNow.getDateRefresh());
    }

    @Test
    void shouldAllowUpdatingDateLogin() {
        LoggedNow loggedNow = new LoggedNow();
        LocalDateTime newDateLogin = LocalDateTime.now();

        loggedNow.setDateLogin(newDateLogin);

        assertEquals(newDateLogin, loggedNow.getDateLogin());
    }

    @Test
    void shouldAllowSettingUseRefreshToFalse() {
        LoggedNow loggedNow = new LoggedNow();
        loggedNow.setUseRefresh(false);

        assertFalse(loggedNow.isUseRefresh());
    }

    @Test
    void shouldHandleNullValuesForOptionalFields() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID tokenLogId = UUID.randomUUID();

        LoggedNow loggedNow = new LoggedNow(id, userId, tokenLogId, null, false, null);

        assertEquals(id, loggedNow.getId());
        assertEquals(userId, loggedNow.getUserId());
        assertEquals(tokenLogId, loggedNow.getTokenLogId());
        assertNull(loggedNow.getDateLogin());
        assertFalse(loggedNow.isUseRefresh());
        assertNull(loggedNow.getDateRefresh());
    }
}