package br.com.hahn.auth.infrastructure.security;

import br.com.hahn.auth.application.execption.KeyRotationException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Getter
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

    private void rotateKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair newKeyPair = keyGen.generateKeyPair();
            String keyId = UUID.randomUUID().toString();
            currentKeyPair.set(newKeyPair);
            currentKeyId.set(keyId);
            publicKeys.put(keyId, newKeyPair.getPublic());
        } catch (Exception _) {
            throw new KeyRotationException("Failed to rotate keys");
        }
    }

    public String getPublicKeyAsPEM(String keyId) {
        PublicKey publicKey = publicKeys.get(keyId);
        if (publicKey == null) return null;
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}