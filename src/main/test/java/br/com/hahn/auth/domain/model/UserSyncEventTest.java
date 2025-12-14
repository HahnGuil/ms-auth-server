package br.com.hahn.auth.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserSyncEventTest {

    @Test
    void shouldCreateUserSyncEventWithAllFields() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        String applicationCode = "APP001";

        UserSyncEvent event = new UserSyncEvent();
        event.setUuid(uuid);
        event.setApplicationCode(applicationCode);

        assertEquals(uuid, event.getUuid());
        assertEquals(applicationCode, event.getApplicationCode());
    }

    @Test
    void shouldAllowUpdatingUuid() {
        UserSyncEvent event = new UserSyncEvent();
        String newUuid = "987e6543-e21b-34d3-c456-426614174999";

        event.setUuid(newUuid);

        assertEquals(newUuid, event.getUuid());
    }

    @Test
    void shouldAllowSettingApplicationCodeToNull() {
        UserSyncEvent event = new UserSyncEvent();
        event.setApplicationCode(null);

        assertNull(event.getApplicationCode());
    }

    @Test
    void shouldHandleNullValuesForAllFields() {
        UserSyncEvent event = new UserSyncEvent();
        event.setUuid(null);
        event.setApplicationCode(null);

        assertNull(event.getUuid());
        assertNull(event.getApplicationCode());
    }
}