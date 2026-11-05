package br.com.hahn.auth.infrastructure.security.cors;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CorsConfigTest {
    @Test void registersCorsMapping() {
        CorsRegistry registry = new CorsRegistry();
        assertDoesNotThrow(() -> new CorsConfig().addCorsMappings(registry));
    }
}
