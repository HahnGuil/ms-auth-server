package br.com.hahn.auth.infrastructure.service;

import br.com.hahn.auth.application.execption.InvalidOperationException;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class UserDataClient {

    private final WebClient webClient;

    @Value("${integration.toxicbet.users.patch-user-email-path:/users}")
    private String patchUserEmailPath;

    public UserDataClient(WebClient.Builder webClientBuilder,
                          @Value("${integration.toxicbet.users.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public void patchUserByEmailHeader(String bearerToken, String userEmail) {
        log.info("UserDataClient: Calling patch user endpoint for email {} at {}", userEmail, DateTimeConverter.formatInstantNow());

        webClient.patch()
                .uri(patchUserEmailPath)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, buildAuthorizationHeader(bearerToken))
                .header("userEmail", userEmail)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(responseBody -> new InvalidOperationException(
                                "Error updating user via external service. HTTP "
                                        + response.statusCode().value()
                                        + " - "
                                        + responseBody)))
                .toBodilessEntity()
                .block();
    }

    private String buildAuthorizationHeader(String token) {
        if (token.startsWith("Bearer ")) {
            return token;
        }

        return "Bearer " + token;
    }
}