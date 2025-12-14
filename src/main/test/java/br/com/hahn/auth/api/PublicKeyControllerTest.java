package br.com.hahn.auth.api;

import br.com.hahn.auth.application.dto.JwkKey;
import br.com.hahn.auth.application.dto.response.JWTsResponse;
import br.com.hahn.auth.infrastructure.security.KeyManager;
import org.junit.jupiter.api.Test;

import java.security.PublicKey;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PublicKeyControllerTest {

    private final KeyManager keyManager = mock(KeyManager.class);
    private final PublicKeyController controller = new PublicKeyController(keyManager);

    @Test
    void shouldReturnJWTsResponseWithKeys() {
        JwkKey jwkKey = mock(JwkKey.class);
        JWTsResponse expectedResponse = new JWTsResponse(Collections.singletonList(jwkKey));
        when(keyManager.getJwkKeys()).thenReturn(Collections.singletonList(jwkKey));

        JWTsResponse response = controller.getJwks();

        assertEquals(expectedResponse.getKeys(), response.getKeys());
        verify(keyManager, times(1)).getJwkKeys();
    }

    @Test
    void shouldReturnEmptyJWTsResponseWhenNoKeysAvailable() {
        when(keyManager.getJwkKeys()).thenReturn(Collections.emptyList());

        JWTsResponse response = controller.getJwks();

        assertEquals(0, response.getKeys().size());
        verify(keyManager, times(1)).getJwkKeys();
    }

    @Test
    void shouldReturnPublicKeysInLegacyFormat() {
        PublicKey publicKey = mock(PublicKey.class);
        Map<String, String> expectedKeys = Map.of("kid1", "PEM_KEY_1");
        when(keyManager.getPublicKeys()).thenReturn(Map.of("kid1", publicKey));
        when(keyManager.getPublicKeyAsPEM("kid1")).thenReturn("PEM_KEY_1");

        Map<String, String> response = controller.getPublicKeys();

        assertEquals(expectedKeys, response);
        verify(keyManager, times(1)).getPublicKeys();
        verify(keyManager, times(1)).getPublicKeyAsPEM("kid1");
    }

    @Test
    void shouldReturnEmptyMapWhenNoPublicKeysAvailable() {
        when(keyManager.getPublicKeys()).thenReturn(Collections.emptyMap());

        Map<String, String> response = controller.getPublicKeys();

        assertEquals(0, response.size());
        verify(keyManager, times(1)).getPublicKeys();
    }
}