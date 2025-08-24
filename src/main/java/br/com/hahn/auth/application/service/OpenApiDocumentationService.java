package br.com.hahn.auth.application.service;

import br.com.hahn.auth.infrastructure.configuration.SimpleHttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.webmvc.api.OpenApiResource;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

@Service
public class OpenApiDocumentationService {

    private static final Logger logge = LoggerFactory.getLogger(OpenApiDocumentationService.class);

    private final OpenApiResource openApiResource;
    private final ObjectMapper objectMapper; // Jackson ObjectMapper

    public OpenApiDocumentationService(OpenApiResource openApiResource) {
        this.openApiResource = openApiResource;
        this.objectMapper = new ObjectMapper();
    }

    public void saveOpenApiDocumentation() {
        logge.info("OpenApiDocumentationService: save api documentation");
        try {
            logge.info("OpenApiDocmentationService: create simulated HttpServeletRequest");
            SimpleHttpServletRequest request = new SimpleHttpServletRequest();

            logge.info("OpenApiDocmentationService: Generate json from OpenAPI");
            byte[] jsonBytes = openApiResource.openapiJson(request, "/v3/api-docs", Locale.getDefault());
            String jsonDocs = new String(jsonBytes, StandardCharsets.UTF_8);

            logge.info("OpenApiDocmentationService: Formate json");
            Object jsonObject = objectMapper.readValue(jsonDocs, Object.class); // Convert JSON string to an object
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject); // Format the JSON

            logge.info("OpenApiDocmentationService: Create docs folder, if doesn't exist");
            if (!Files.exists(Paths.get("docs/swagger"))) {
                Files.createDirectories(Paths.get("docs/swagger"));
            }

            logge.info("OpenApiDocmentationService: Save JSON file");
            try (FileWriter fileWriter = new FileWriter("docs/swagger/auth-api.json")) {
                fileWriter.write(prettyJson);
            }


            logge.info("OpenApiDocmentationService: Swagger documentation saved to docs/swagger/auth-api.json");
        } catch (IOException e) {
            logge.info("OpenApiDocmentationService: Error to save documentation {}", e.getMessage());
        }
    }
}
