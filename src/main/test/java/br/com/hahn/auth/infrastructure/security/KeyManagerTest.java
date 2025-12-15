package br.com.hahn.auth.infrastructure.security;

import br.com.hahn.auth.application.dto.JwkKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class KeyManagerTest {

    private KeyManager keyManager;

    @BeforeEach
    void setUp() throws Exception {
        keyManager = new KeyManager();
        invokeRotateKeys();
    }

    private void invokeRotateKeys() throws Exception {
        Method rotateKeysMethod = KeyManager.class.getDeclaredMethod("rotateKeys");
        rotateKeysMethod.setAccessible(true);
        rotateKeysMethod.invoke(keyManager);
    }

    @Test
    void shouldRotateKeysAndUpdateCurrentKeyPairAndKeyId() {
        assertNotNull(keyManager.getCurrentKeyPair());
        assertNotNull(keyManager.getCurrentKeyId());
        assertTrue(keyManager.getPublicKeys().containsKey(keyManager.getCurrentKeyId()));
    }

    @Test
    void shouldReturnPublicKeyAsPEMWhenKeyIdExists() {
        String keyId = keyManager.getCurrentKeyId();

        String pem = keyManager.getPublicKeyAsPEM(keyId);

        assertNotNull(pem);
        assertFalse(pem.isEmpty());
    }

    @Test
    void shouldReturnNullWhenKeyIdDoesNotExist() {
        String pem = keyManager.getPublicKeyAsPEM("nonexistentKeyId");

        assertNull(pem);
    }

    @Test
    void shouldReturnJwkKeysForAllStoredPublicKeys() {

        List<JwkKey> jwkKeys = keyManager.getJwkKeys();

        assertNotNull(jwkKeys);
        assertFalse(jwkKeys.isEmpty());
        assertEquals(1, jwkKeys.size());
        assertEquals(keyManager.getCurrentKeyId(), jwkKeys.getFirst().getKeyId());
    }

    @Test
    void shouldReturnEmptyJwkKeysWhenNoPublicKeysExist(){
        KeyManager emptyKeyManager = new KeyManager();

        List<JwkKey> jwkKeys = emptyKeyManager.getJwkKeys();

        assertNotNull(jwkKeys);
        assertTrue(jwkKeys.isEmpty());
    }
}