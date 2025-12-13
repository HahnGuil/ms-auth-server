package br.com.hahn.auth.api;

import br.com.hahn.auth.application.dto.response.JWTsResponse;
import br.com.hahn.auth.infrastructure.security.KeyManager;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public-key")
@RequiredArgsConstructor
public class PublicKeyController {

    private final KeyManager keyManager;
    private static final Logger logger = LoggerFactory.getLogger(PublicKeyController.class);

    /**
     * Returns the JSON Web Key Set (JWKS) containing the public keys used to verify JWT signatures.
     * <p>
     * This endpoint exposes the JWKS in the standard JSON Web Key Set format so clients
     * can retrieve the public keys necessary to validate tokens issued by this server.
     * </p>
     *
     * @author HahnGuil
     * @return a {@link JWTsResponse} containing the collection of JWK keys managed by the KeyManager
     */
    @GetMapping("/jwks")
    public JWTsResponse getJwks() {
        logger.info("PublicKeyController: get JWKS at: {}", DateTimeConverter.formatInstantNow());
        return new JWTsResponse(keyManager.getJwkKeys());
    }

    /**
     * Retrieves the public keys in a legacy format.
     * <p>
     * This endpoint returns a map where the keys are key identifiers (kid) and the values
     * are the corresponding public keys in PEM format. It is intended for compatibility
     * with systems that require the legacy format.
     * </p>
     *
     * @return a {@link Map} containing key identifiers as keys and public keys in PEM format as values
     */
    @GetMapping("/legacy")
    public Map<String, String> getPublicKeys() {
        logger.info("PublicKeyController: get public key (legacy format) at: {}", DateTimeConverter.formatInstantNow());
        return keyManager.getPublicKeys().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> keyManager.getPublicKeyAsPEM(entry.getKey())
                ));
    }
}