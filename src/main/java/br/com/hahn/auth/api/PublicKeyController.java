package br.com.hahn.auth.api;

import br.com.hahn.auth.infrastructure.security.KeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public-key")
public class PublicKeyController {
    
    private final KeyManager keyManager;

    private static final Logger logger = LoggerFactory.getLogger(PublicKeyController.class);
    
    public PublicKeyController(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    @GetMapping("/jwks")
    public Map<String, String> getPublicKeys() {
        logger.info("PublicKeyController: get public key");
        return keyManager.getPublicKeys().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> keyManager.getPublicKeyAsPEM(entry.getKey())
                ));
    }
    
}
