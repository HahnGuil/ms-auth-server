package br.com.hahn.auth.domain.model;

import br.com.hahn.auth.domain.enums.TypeUser;
import br.com.hahn.auth.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {


    @Test
    void shouldCreateOAuthUserWithAllFields() {
        UUID userId = UUID.randomUUID();
        String username = "testUser";
        String password = "password123";
        LocalDateTime passwordCreateDate = LocalDateTime.now();
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String pictureUrl = "OauthUser-image-link";
        Boolean blockUser = false;
        TypeUser typeUser = TypeUser.OAUTH_USER;
        UserRole role = UserRole.USER_NORMAL;

        User user = new User(userId, username, password, passwordCreateDate, email, firstName, lastName, pictureUrl, blockUser, typeUser, role, null, null);

        assertEquals(userId, user.getUserId());
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        assertEquals(passwordCreateDate, user.getPasswordCreateDate());
        assertEquals(email, user.getEmail());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(pictureUrl, user.getPictureUrl());
        assertFalse(user.getBlockUser());
        assertEquals(typeUser, user.getTypeUser());
        assertEquals(role, user.getRole());
    }

    @Test
    void shouldCreateDirectlyUserWithAllFields() {
        UUID userId = UUID.randomUUID();
        String username = "testUser";
        String password = "password123";
        LocalDateTime passwordCreateDate = LocalDateTime.now();
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String pictureUrl = "Direct-image-link";
        Boolean blockUser = false;
        TypeUser typeUser = TypeUser.DIRECT_USER;
        UserRole role = UserRole.USER_NORMAL;

        User user = new User(userId, username, password, passwordCreateDate, email, firstName, lastName, pictureUrl, blockUser, typeUser, role, null, null);

        assertEquals(userId, user.getUserId());
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        assertEquals(passwordCreateDate, user.getPasswordCreateDate());
        assertEquals(email, user.getEmail());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(pictureUrl, user.getPictureUrl());
        assertFalse(user.getBlockUser());
        assertEquals(typeUser, user.getTypeUser());
        assertEquals(role, user.getRole());
    }

    @Test
    void shouldAllowUpdatingEmail() {
        User user = new User();
        String newEmail = "newemail@example.com";

        user.setEmail(newEmail);

        assertEquals(newEmail, user.getEmail());
    }

    @Test
    void shouldAllowSettingBlockUserToTrue() {
        User user = new User();
        user.setBlockUser(true);

        assertTrue(user.getBlockUser());
    }

    @Test
    void shouldHandleNullValuesForOptionalFields() {
        User user = new User(null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertNull(user.getUserId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getPasswordCreateDate());
        assertNull(user.getEmail());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getPictureUrl());
        assertNull(user.getBlockUser());
        assertNull(user.getTypeUser());
        assertNull(user.getRole());
        assertNull(user.getTokenLogs());
        assertNull(user.getApplications());
    }

    @Test
    void shouldVerifyPasswordCreateDateIsInPast() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        User user = new User();
        user.setPasswordCreateDate(pastDate);

        assertTrue(user.getPasswordCreateDate().isBefore(LocalDateTime.now()));
    }
}