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
