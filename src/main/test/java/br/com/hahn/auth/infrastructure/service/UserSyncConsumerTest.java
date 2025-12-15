package br.com.hahn.auth.infrastructure.service;

import br.com.hahn.auth.application.service.UserSyncService;
import br.com.hahn.auth.domain.model.UserSyncEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class UserSyncConsumerTest {

    private UserSyncConsumer userSyncConsumer;
    private UserSyncService userSyncService;

    @BeforeEach
    void setUp() {
        userSyncService = mock(UserSyncService.class);
        userSyncConsumer = new UserSyncConsumer(userSyncService);
    }

    @Nested
    @DisplayName("consume")
    class Consume {

        @Test
        @DisplayName("Should call UserSyncService with valid event")
        void shouldCallUserSyncServiceWithValidEvent() {
            UserSyncEvent event = new UserSyncEvent();
            event.setUuid("uuid-123");
            event.setApplicationCode("app-456");

            userSyncConsumer.consume(event);

            verify(userSyncService).syncUser("uuid-123", "app-456");
        }

        @Test
        @DisplayName("Should log synchronization details")
        void shouldLogSynchronizationDetails() {
            UserSyncEvent event = new UserSyncEvent();
            event.setUuid("uuid-789");
            event.setApplicationCode("app-012");

            userSyncConsumer.consume(event);

            verify(userSyncService).syncUser("uuid-789", "app-012");
            verifyNoMoreInteractions(userSyncService);
        }

        @Test
        @DisplayName("Should handle event with null uuid")
        void shouldHandleEventWithNullUuid() {
            UserSyncEvent event = new UserSyncEvent();
            event.setUuid(null);
            event.setApplicationCode("app-456");

            userSyncConsumer.consume(event);

            verify(userSyncService).syncUser(null, "app-456");
        }

        @Test
        @DisplayName("Should handle event with null application code")
        void shouldHandleEventWithNullApplicationCode() {
            UserSyncEvent event = new UserSyncEvent();
            event.setUuid("uuid-123");
            event.setApplicationCode(null);

            userSyncConsumer.consume(event);

            verify(userSyncService).syncUser("uuid-123", null);
        }

        @Test
        @DisplayName("Should handle event with empty strings")
        void shouldHandleEventWithEmptyStrings() {
            UserSyncEvent event = new UserSyncEvent();
            event.setUuid("");
            event.setApplicationCode("");

            userSyncConsumer.consume(event);

            verify(userSyncService).syncUser("", "");
        }
    }
}