package br.com.hahn.auth.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class JwtDecoderConfig {

    private final KeyManager keyManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, JwtDecoder> decodersCache = new ConcurrentHashMap<>();

    /**
     * Creates a JwtDecoder bean for decoding JWT tokens.
     *
     * <p>This method provides a lambda-based implementation for decoding JWT tokens.
     * It parses the token, extracts the "kid" (Key ID) from the header, and retrieves
     * or creates a cached JwtDecoder instance based on the "kid". If no "kid" is present,
     * a default decoder is used. The decoder uses an RSA public key for validation.</p>
     *
     * <p>Key steps:</p>
     * <ul>
     *   <li>Splits the token into its parts (header, payload, signature).</li>
     *   <li>Decodes the header and extracts the "kid" field if present.</li>
     *   <li>Uses a cache to store and retrieve JwtDecoder instances based on the "kid".</li>
     *   <li>Resolves the RSA public key for the decoder using the "kid" or a default key.</li>
     *   <li>Handles exceptions for invalid tokens or decoding errors.</li>
     * </ul>
     *
     * @author HahnGuil
     * @return a JwtDecoder instance for decoding JWT tokens
     * @throws JwtException if the token is invalid or cannot be decoded
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> {
            try {
                String[] parts = token.split("\\.");
                if (parts.length < 2) {
                    throw new JwtException("Invalid JWT token");
                }

                String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
                JsonNode headerNode = objectMapper.readTree(headerJson);
                String kid = headerNode.has("kid") ? headerNode.get("kid").asText() : null;

                String cacheKey = kid == null ? "default" : kid;

                JwtDecoder decoder = decodersCache.computeIfAbsent(cacheKey, k -> {
                    // use o parâmetro 'k' do lambda (final/efetivamente final) em vez de 'kid'
                    RSAPublicKey publicKey = resolvePublicKey("default".equals(k) ? null : k);
                    return NimbusJwtDecoder.withPublicKey(publicKey).build();
                });

                return decoder.decode(token);
            } catch (JwtException e) {
                log.error("JwtDecoderConfig: JwtException ao decodificar token", e);
                throw e;
            } catch (Exception e) {
                log.error("JwtDecoderConfig: Erro ao decodificar token", e);
                throw new JwtException("Unable to decode JWT", e);
            }
        };
    }

    /**
     * Resolves the RSA public key to be used for JWT decoding.
     *
     * <p>This method attempts to retrieve an RSA public key based on the provided Key ID (kid).
     * If the kid is not provided or the corresponding key is not found, it falls back to the
     * current RSA public key managed by the KeyManager.</p>
     *
     * <p>Key steps:</p>
     * <ul>
     *   <li>If a kid is provided, it looks up the public key in the KeyManager's public keys map.</li>
     *   <li>Checks if the retrieved key is an instance of RSAPublicKey.</li>
     *   <li>If no valid key is found for the kid, logs a warning and falls back to the current key pair.</li>
     *   <li>Ensures the fallback key pair contains a valid RSA public key.</li>
     * </ul>
     *
     * @author HahnGuil
     * @param kid the Key ID used to look up the public key (can be null)
     * @return the resolved RSAPublicKey
     * @throws IllegalStateException if no valid RSA public key is available
     */
    private RSAPublicKey resolvePublicKey(String kid) {
        if (kid != null) {
            PublicKey maybeKey = keyManager.getPublicKeys().get(kid);
            if (maybeKey instanceof RSAPublicKey rsaKey) {
                return rsaKey;
            }
            log.warn("JwtDecoderConfig: Public key for kid {} not found or not RSA, falling back to current key", kid);
        }

        KeyPair kp = keyManager.getCurrentKeyPair();
        if (kp == null || !(kp.getPublic() instanceof RSAPublicKey)) {
            log.error("JwtDecoderConfig: No available RSA public key");
            throw new IllegalStateException("Public key not available for JwtDecoder");
        }
        return (RSAPublicKey) kp.getPublic();
    }
}