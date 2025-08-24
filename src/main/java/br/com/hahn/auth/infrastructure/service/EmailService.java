package br.com.hahn.auth.infrastructure.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final WebClient webClient;

    public EmailService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.resend.com").build();
    }

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.api.url}")
    private String apiUrl;

    @Value("${resend.sender}")
    private String sender;

    public Mono<Void> sendEmail(String to, String subject, String corpoHtml) {
        logger.info("EmailService: Send email.");
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
