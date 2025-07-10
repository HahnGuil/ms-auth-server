package br.com.hahn.auth.infrastructure.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class EmailService {

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

    public Mono<Void> enviarEmail(String to, String assunto, String corpoHtml) {
        return webClient.post()
                .uri("/emails")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "from", sender,
                        "to", to,
                        "subject", assunto,
                        "html", corpoHtml
                ))
                .retrieve()
                .bodyToMono(String.class)
                .then();
    }

}
