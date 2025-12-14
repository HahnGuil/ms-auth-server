package br.com.hahn.auth.domain.model;

import br.com.hahn.auth.domain.enums.TypeInvalidation;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InvalidatedTokenTest {

    @Test
    void shouldCreateInvalidatedTokenWithAllFields_whenTypeInvalidationIsExpirationTime() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID loginLogId = UUID.randomUUID();
        LocalDateTime dateInvalidate = LocalDateTime.now();
        TypeInvalidation typeInvalidation = TypeInvalidation.EXPIRATION_TIME;

        InvalidatedToken token = new InvalidatedToken(id, userId, loginLogId, dateInvalidate, typeInvalidation);

        assertEquals(id, token.getId());
        assertEquals(userId, token.getUserId());
        assertEquals(loginLogId, token.getLoginLogId());
        assertEquals(dateInvalidate, token.getDateInvalidate());
        assertEquals(typeInvalidation, token.getTypeInvalidation());
    }

    @Test
    void shouldCreateInvalidatedTokenWithAllFields_whenTypeInvalidationIsUserRefresh() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID loginLogId = UUID.randomUUID();
        LocalDateTime dateInvalidate = LocalDateTime.now();
        TypeInvalidation typeInvalidation = TypeInvalidation.USER_REFRESH;

        InvalidatedToken token = new InvalidatedToken(id, userId, loginLogId, dateInvalidate, typeInvalidation);

        assertEquals(id, token.getId());
        assertEquals(userId, token.getUserId());
        assertEquals(loginLogId, token.getLoginLogId());
        assertEquals(dateInvalidate, token.getDateInvalidate());
        assertEquals(typeInvalidation, token.getTypeInvalidation());
    }

    @Test
    void shouldCreateInvalidatedTokenWithAllFields_whenTypeInvalidationIsLog_Off() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID loginLogId = UUID.randomUUID();
        LocalDateTime dateInvalidate = LocalDateTime.now();
        TypeInvalidation typeInvalidation = TypeInvalidation.LOG_OFF;

        InvalidatedToken token = new InvalidatedToken(id, userId, loginLogId, dateInvalidate, typeInvalidation);

        assertEquals(id, token.getId());
        assertEquals(userId, token.getUserId());
        assertEquals(loginLogId, token.getLoginLogId());
        assertEquals(dateInvalidate, token.getDateInvalidate());
        assertEquals(typeInvalidation, token.getTypeInvalidation());
    }

    @Test
    void shouldCreateInvalidatedTokenWithAllFields_whenTypeInvalidationIsNewLogin() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID loginLogId = UUID.randomUUID();
        LocalDateTime dateInvalidate = LocalDateTime.now();
        TypeInvalidation typeInvalidation = TypeInvalidation.NEW_LOGIN;

        InvalidatedToken token = new InvalidatedToken(id, userId, loginLogId, dateInvalidate, typeInvalidation);

        assertEquals(id, token.getId());
        assertEquals(userId, token.getUserId());
        assertEquals(loginLogId, token.getLoginLogId());
        assertEquals(dateInvalidate, token.getDateInvalidate());
        assertEquals(typeInvalidation, token.getTypeInvalidation());
    }

    @Test
    void shouldCreateInvalidatedTokenWithAllFields_whenTypeInvalidationIsChangePassword() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID loginLogId = UUID.randomUUID();
        LocalDateTime dateInvalidate = LocalDateTime.now();
        TypeInvalidation typeInvalidation = TypeInvalidation.CHANGE_PASSWORD;

        InvalidatedToken token = new InvalidatedToken(id, userId, loginLogId, dateInvalidate, typeInvalidation);

        assertEquals(id, token.getId());
        assertEquals(userId, token.getUserId());
        assertEquals(loginLogId, token.getLoginLogId());
        assertEquals(dateInvalidate, token.getDateInvalidate());
        assertEquals(typeInvalidation, token.getTypeInvalidation());
    }

    @Test
    void shouldCreateInvalidatedTokenWithAllFields_whenTypeInvalidationIsResetPassword() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID loginLogId = UUID.randomUUID();
        LocalDateTime dateInvalidate = LocalDateTime.now();
        TypeInvalidation typeInvalidation = TypeInvalidation.RESET_PASSWORD;

        InvalidatedToken token = new InvalidatedToken(id, userId, loginLogId, dateInvalidate, typeInvalidation);

        assertEquals(id, token.getId());
        assertEquals(userId, token.getUserId());
        assertEquals(loginLogId, token.getLoginLogId());
        assertEquals(dateInvalidate, token.getDateInvalidate());
        assertEquals(typeInvalidation, token.getTypeInvalidation());
    }

    @Test
    void shouldAllowUpdatingDateInvalidate() {
        InvalidatedToken token = new InvalidatedToken();
        LocalDateTime newDate = LocalDateTime.now();

        token.setDateInvalidate(newDate);

        assertEquals(newDate, token.getDateInvalidate());
    }

    @Test
    void shouldAllowSettingTypeInvalidationToNull() {
        InvalidatedToken token = new InvalidatedToken();
        token.setTypeInvalidation(null);

        assertNull(token.getTypeInvalidation());
    }

    @Test
    void shouldHandleNullValuesForOptionalFields() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID loginLogId = UUID.randomUUID();

        InvalidatedToken token = new InvalidatedToken(id, userId, loginLogId, null, null);

        assertEquals(id, token.getId());
        assertEquals(userId, token.getUserId());
        assertEquals(loginLogId, token.getLoginLogId());
        assertNull(token.getDateInvalidate());
        assertNull(token.getTypeInvalidation());
    }
}