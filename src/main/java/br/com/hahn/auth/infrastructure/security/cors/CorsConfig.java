package br.com.hahn.auth.infrastructure.security.cors;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Configure CORS mappings for the application.
     *
     * <p>This method sets up Cross-Origin Resource Sharing (CORS) configurations
     * to allow requests from specific local origins. It defines the allowed HTTP
     * methods, headers, and whether credentials are supported.</p>
     *
     * <ul>
     *   <li><b>Allowed Origins:</b> Requests are permitted from "http://localhost:4200"
     *   and "http://localhost:5173".</li>
     *   <li><b>Allowed Methods:</b> Supports GET, POST, DELETE, PUT, PATCH, and OPTIONS.</li>
     *   <li><b>Allowed Headers:</b> Includes Authorization, Content-Type, Accept, and any other headers.</li>
     *   <li><b>Credentials:</b> Disabled, meaning cookies or other credentials will not be sent.</li>
     * </ul>
     *
     * @param registry the CorsRegistry used to register CORS mappings
     * @author HahnGuil
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200", "http://localhost:5173")
                .allowedMethods("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept", "*")
                .allowCredentials(false);
    }
}
