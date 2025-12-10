package br.com.hahn.auth.infrastructure.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final WebClient webClient;

    public EmailService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.resend.com").build();
    }

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.sender}")
    private String sender;

    public Mono<Void> sendEmail(String to, String subject, String corpoHtml) {
        log.info("EmailService: Send email to: {}, at: {}", to, Instant.now());
        return webClient.post()
                .uri("/emails")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "from", sender,
                        "to", to,
                        "subject", subject,
                        "html", corpoHtml
                ))
                .retrieve()
                .bodyToMono(String.class)
                .then();
    }
}
