package br.com.hahn.auth;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
@AllArgsConstructor
public class MsAuthServerApplication {

    private final Environment env;
    private final ApplicationContext applicationContext;

    private static final String LINE_DIVISOR = "-----------------\n";

    public static void main(String[] args) {
        try {
            SpringApplication.run(MsAuthServerApplication.class, args);
        } catch (Exception e) {
            log.error("MsAuthServerApplication: Error starting application: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void printStartupInfo() {
        String contextPath = env.getProperty("server.servlet.context-path", "");
        int port = ((ServletWebServerApplicationContext) applicationContext).getWebServer().getPort();
        String baseUrl = "http://localhost:" + port + contextPath;
        String healthUrl = baseUrl + "/actuator/health";
        String swaggerUrl = baseUrl + "/swagger-ui/index.html";

        log.info(LINE_DIVISOR);
        log.info("Application Started\n");
        log.info("Context Path {}", contextPath);
        log.info("Port {}", port);
        log.info("Health {}", healthUrl);
        log.info("Swagger {}", swaggerUrl);
        log.info(LINE_DIVISOR);
    }
}
