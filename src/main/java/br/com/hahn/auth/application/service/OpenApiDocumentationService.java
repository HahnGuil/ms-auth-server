package br.com.hahn.auth.application.service;

import br.com.hahn.auth.infrastructure.configuration.SimpleHttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final OpenApiResource openApiResource;
    private final ObjectMapper objectMapper; // Jackson ObjectMapper

    public OpenApiDocumentationService(OpenApiResource openApiResource) {
        this.openApiResource = openApiResource;
        this.objectMapper = new ObjectMapper();
    }

    public void saveOpenApiDocumentation() {
        try {
            // Create a simulated HttpServletRequest
            SimpleHttpServletRequest request = new SimpleHttpServletRequest();

            // Generate JSON from OpenAPI documentation
            byte[] jsonBytes = openApiResource.openapiJson(request, "/v3/api-docs", Locale.getDefault());
            String jsonDocs = new String(jsonBytes, StandardCharsets.UTF_8);

            // Format json
            Object jsonObject = objectMapper.readValue(jsonDocs, Object.class); // Convert JSON string to an object
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject); // Format the JSON

            // Create the "docs" folder if it doesn't exist
            if (!Files.exists(Paths.get("docs/swagger"))) {
                Files.createDirectories(Paths.get("docs/swagger"));
            }

            // Save de JSON file
            FileWriter fileWriter = new FileWriter("docs/swagger/auth-api.json");
            fileWriter.write(prettyJson);
            fileWriter.close();

            System.out.println("Swagger documentation saved to docs/swagger/auth-api.json");
        } catch (IOException e) {
            System.err.println("Error to save documentation: " + e.getMessage());
        }
    }
}
