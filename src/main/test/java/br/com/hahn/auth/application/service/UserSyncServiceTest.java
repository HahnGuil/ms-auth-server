package br.com.hahn.auth.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class UserSyncServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserSyncService userSyncService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("syncUser")
    class SyncUser {

        @Test
        @DisplayName("Should sync user and add application successfully")
        void syncUserSuccessfully() {
            String uuid = UUID.randomUUID().toString();
            String applicationCode = "123";
            UUID expectedUserId = UUID.fromString(uuid);
            Long expectedApplicationId = 123L;

            doNothing().when(userService).setApplicationToUser(any(UUID.class), anyLong());

            userSyncService.syncUser(uuid, applicationCode);

            verify(userService).setApplicationToUser(expectedUserId, expectedApplicationId);
        }

        @Test
        @DisplayName("Should throw exception when UUID is invalid")
        void syncUserThrowsExceptionForInvalidUUID() {
            String invalidUuid = "invalid-uuid";
            String applicationCode = "123";

            assertThrows(IllegalArgumentException.class, () -> userSyncService.syncUser(invalidUuid, applicationCode));
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("Should throw exception when application code is invalid")
        void syncUserThrowsExceptionForInvalidApplicationCode() {
            String uuid = UUID.randomUUID().toString();
            String invalidApplicationCode = "invalid-code";

            assertThrows(NumberFormatException.class, () -> userSyncService.syncUser(uuid, invalidApplicationCode));
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("Should throw exception when UUID is null")
        void syncUserThrowsExceptionForNullUUID() {
            String nullUuid = null;
            String applicationCode = "123";

            assertThrows(NullPointerException.class, () -> userSyncService.syncUser(nullUuid, applicationCode));
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("Should throw exception when application code is null")
        void syncUserThrowsExceptionForNullApplicationCode() {
            String uuid = UUID.randomUUID().toString();
            String nullApplicationCode = null;

            assertThrows(NumberFormatException.class, () -> userSyncService.syncUser(uuid, nullApplicationCode));
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("Should sync user with valid UUID in lowercase")
        void syncUserWithLowercaseUUID() {
            UUID testUuid = UUID.randomUUID();
            String uuid = testUuid.toString().toLowerCase();
            String applicationCode = "456";
            Long expectedApplicationId = 456L;

            doNothing().when(userService).setApplicationToUser(any(UUID.class), anyLong());

            userSyncService.syncUser(uuid, applicationCode);

            verify(userService).setApplicationToUser(testUuid, expectedApplicationId);
        }

        @Test
        @DisplayName("Should sync user with valid UUID in uppercase")
        void syncUserWithUppercaseUUID() {
            UUID testUuid = UUID.randomUUID();
            String uuid = testUuid.toString().toUpperCase();
            String applicationCode = "789";
            Long expectedApplicationId = 789L;

            doNothing().when(userService).setApplicationToUser(any(UUID.class), anyLong());

            userSyncService.syncUser(uuid, applicationCode);

            verify(userService).setApplicationToUser(testUuid, expectedApplicationId);
        }

        @Test
        @DisplayName("Should sync user with large application code")
        void syncUserWithLargeApplicationCode() {
            String uuid = UUID.randomUUID().toString();
            String applicationCode = "9999999999";
            UUID expectedUserId = UUID.fromString(uuid);
            Long expectedApplicationId = 9999999999L;

            doNothing().when(userService).setApplicationToUser(any(UUID.class), anyLong());

            userSyncService.syncUser(uuid, applicationCode);

            verify(userService).setApplicationToUser(expectedUserId, expectedApplicationId);
        }

        @Test
        @DisplayName("Should throw exception when application code is empty")
        void syncUserThrowsExceptionForEmptyApplicationCode() {
            String uuid = UUID.randomUUID().toString();
            String emptyApplicationCode = "";

            assertThrows(NumberFormatException.class, () -> userSyncService.syncUser(uuid, emptyApplicationCode));
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("Should throw exception when UUID is empty")
        void syncUserThrowsExceptionForEmptyUUID() {
            String emptyUuid = "";
            String applicationCode = "123";

            assertThrows(IllegalArgumentException.class, () -> userSyncService.syncUser(emptyUuid, applicationCode));
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("Should throw exception when application code contains non-numeric characters")
        void syncUserThrowsExceptionForNonNumericApplicationCode() {
            String uuid = UUID.randomUUID().toString();
            String nonNumericApplicationCode = "abc123";

            assertThrows(NumberFormatException.class, () -> userSyncService.syncUser(uuid, nonNumericApplicationCode));
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("Should throw exception when UUID has invalid format")
        void syncUserThrowsExceptionForInvalidUUIDFormat() {
            String invalidFormatUuid = "123-456-789";
            String applicationCode = "123";

            assertThrows(IllegalArgumentException.class, () -> userSyncService.syncUser(invalidFormatUuid, applicationCode));
            verifyNoInteractions(userService);
        }
    }
}
