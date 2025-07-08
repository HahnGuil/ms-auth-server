package br.com.hahn.auth.infrastructure.security;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.*;

@Service
public class KeyManager {

    private volatile KeyPair currentKeyPair;
    private volatile String currentKeyId;
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
            currentKeyPair = newKeyPair;
            currentKeyId = keyId;
            publicKeys.put(keyId, newKeyPair.getPublic());

        } catch (Exception e) {
            throw new RuntimeException("Rotation keys is fail", e);
        }
    }

    public KeyPair getCurrentKeyPair() {
        return currentKeyPair;
    }

    public String getCurrentKeyId() {
        return currentKeyId;
    }

    public Map<String, PublicKey> getPublicKeys() {
        return publicKeys;
    }

    public String getPublicKeyAsPEM(String keyId) {
        PublicKey publicKey = publicKeys.get(keyId);
        if (publicKey == null) return null;
        String encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----";
    }
}
