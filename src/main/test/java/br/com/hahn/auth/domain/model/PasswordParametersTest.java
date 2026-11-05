package br.com.hahn.auth.domain.model;

import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PasswordParametersTest {
    @Test void retainsIndependentPasswordMetadataForUserAndApplication() {
        User user = new User(); Application app = new Application();
        var parameters = new PasswordParameters();
        UUID id = UUID.randomUUID(); LocalDateTime created = LocalDateTime.now(ZoneId.systemDefault());
        parameters.setId(id); parameters.setUser(user); parameters.setApplication(app);
        parameters.setPassword("app-hash"); parameters.setPasswordCreateDate(created); parameters.setBlockUser(true);
        assertEquals(id, parameters.getId()); assertSame(user, parameters.getUser()); assertSame(app, parameters.getApplication());
        assertEquals("app-hash", parameters.getPassword()); assertEquals(created, parameters.getPasswordCreateDate()); assertTrue(parameters.getBlockUser());
    }
}
