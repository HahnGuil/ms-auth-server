package br.com.hahn.auth.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {

    /**
     * Tests the creation of an Application object with all fields initialized.
     * Verifies that the fields are correctly set and the users set is empty.
     */
    @Test
    void shouldCreateApplicationWithAllFields() {
        Application application = new Application(1L, "TestApp", Set.of());

        assertEquals(1L, application.getId());
        assertEquals("TestApp", application.getNameApplication());
        assertTrue(application.getUsers().isEmpty());
    }

    /**
     * Tests updating the name of an Application object.
     * Verifies that the name is updated correctly.
     */
    @Test
    void shouldAllowUpdatingApplicationName() {
        Application application = new Application();
        application.setNameApplication("UpdatedApp");

        assertEquals("UpdatedApp", application.getNameApplication());
    }

    /**
     * Tests adding users to an Application object.
     * Verifies that the users set contains the added user.
     */
    @Test
    void shouldAllowAddingUsersToApplication() {
        Application application = new Application();
        User user = new User();
        application.setUsers(Set.of(user));

        assertEquals(1, application.getUsers().size());
        assertTrue(application.getUsers().contains(user));
    }

    /**
     * Tests setting the users set of an Application object to null.
     * Verifies that the users set is null.
     */
    @Test
    void shouldHandleNullUsersSet() {
        Application application = new Application();
        application.setUsers(null);

        assertNull(application.getUsers());
    }
}