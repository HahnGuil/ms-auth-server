package br.com.hahn.auth.infrastructure.security;

import br.com.hahn.auth.application.dto.JwkKey;
import br.com.hahn.auth.application.execption.KeyRotationException;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Getter
@RequiredArgsConstructor
@Slf4j
public class KeyManager {

    private final AtomicReference<KeyPair> currentKeyPair = new AtomicReference<>();
    private final AtomicReference<String> currentKeyId = new AtomicReference<>();
    private final Map<String, PublicKey> publicKeys = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        rotateKeys();
        scheduledExecutorService.scheduleAtFixedRate(this::rotateKeys, 2, 2, TimeUnit.HOURS);
    }

    /**
     * Rotates the cryptographic keys used by the application.
     *
     * <p>
     * This method generates a new RSA key pair, creates a unique identifier for the key,
     * updates the atomic references that hold the current key pair and key id, and stores
     * the new public key in the public keys map for distribution (e.g. JWK endpoint).
     * It is intended to be invoked at startup and periodically by a scheduled executor.
     * </p>
     *
     * @throws KeyRotationException if an error occurs during key generation or update
     * @author HahnGuil
     * @since 1.0
     */
    private void rotateKeys() {
        log.info("KeyManager: Starting to rotate keys at: {}", Instant.now());
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair newKeyPair = keyGen.generateKeyPair();
            String keyId = UUID.randomUUID().toString();
            currentKeyPair.set(newKeyPair);
            currentKeyId.set(keyId);
            publicKeys.put(keyId, newKeyPair.getPublic());
        } catch (Exception _) {
            log.error("KayManager: Error to rotate Keys at: {}", Instant.now());
            throw new KeyRotationException(ErrorsResponses.KEY_ROTATION_ERROR.getMessage());
        }
    }

    /**
     * Retrieves the public key in PEM format for the given key ID.
     *
     * <p>
     * This method fetches the public key associated with the provided key ID
     * from the `publicKeys` map. If the key is found, it is encoded in Base64
     * to produce the PEM format. If the key is not found, the method returns null.
     * </p>
     *
     * @author HahnGuil
     * @param keyId the unique identifier of the public key to retrieve
     * @return the public key in PEM format, or null if the key ID does not exist
     */
    public String getPublicKeyAsPEM(String keyId) {
        log.info("KeyManager: Get public Key as PEM at: {}", Instant.now());
        PublicKey publicKey = publicKeys.get(keyId);
        if (publicKey == null) return null;
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Retrieves a list of JSON Web Keys (JWK) representing the public keys managed by this service.
     *
     * <p>
     * This method iterates over the `publicKeys` map, extracting the modulus and exponent
     * from each RSA public key. These values are then used to construct JWK objects, which
     * are added to the resulting list. Only RSA public keys are processed; other key types
     * are ignored.
     * </p>
     *
     * @author HahnGuil
     * @return a list of `JwkKey` objects representing the RSA public keys
     */
    public List<JwkKey> getJwkKeys() {
        log.info("KeyManager: Get JWK Keys at: {}", Instant.now());
        List<JwkKey> jwkKeys = new ArrayList<>();

        publicKeys.forEach((keyId, publicKey) -> {
            if (publicKey instanceof RSAPublicKey rsaPublicKey) {
                String modulus = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(rsaPublicKey.getModulus().toByteArray());
                String exponent = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(rsaPublicKey.getPublicExponent().toByteArray());

                jwkKeys.add(JwkKey.builder()
                        .keyType("RSA")
                        .keyId(keyId)
                        .use("sig")
                        .algorithm("RS256")
                        .modulus(modulus)
                        .exponent(exponent)
                        .build());
            }
        });
        return jwkKeys;
    }

    public KeyPair getCurrentKeyPair() {
        return currentKeyPair.get();
    }

    public String getCurrentKeyId() {
        return currentKeyId.get();
    }
}