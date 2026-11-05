package br.com.hahn.auth.infrastructure.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.core.io.buffer.DataBufferUtils;
import java.nio.charset.StandardCharsets;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.codec.HttpMessageWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class EmailServiceTest {
    @Test void sendsWithPassedApiKeyAndSender() {
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> body = new AtomicReference<>();
        WebClient client = WebClient.builder().exchangeFunction(request -> {
            authorization.set(request.headers().getFirst("Authorization"));
            MockClientHttpRequest output = new MockClientHttpRequest(HttpMethod.POST, request.url());
            output.setWriteHandler(buffers -> DataBufferUtils.join(buffers).doOnNext(buffer -> {
                byte[] bytes = new byte[buffer.readableByteCount()]; buffer.read(bytes); body.set(new String(bytes, StandardCharsets.UTF_8)); DataBufferUtils.release(buffer);
            }).then());
            BodyInserter.Context context = new BodyInserter.Context() {
                @Override public List<HttpMessageWriter<?>> messageWriters() { return ExchangeStrategies.withDefaults().messageWriters(); }
                @Override public Optional<org.springframework.http.server.reactive.ServerHttpRequest> serverRequest() { return Optional.empty(); }
                @Override public Map<String, Object> hints() { return Map.of(); }
            };
            return request.body().insert(output, context).thenReturn(ClientResponse.create(HttpStatus.OK).body("{}").build());
        }).build();
        EmailService service = new EmailService(client.mutate());
        service.sendEmail("passed-key", "custom-sender@example.com", "to@example.com", "Subject", "<p>Hello</p>").block();
        assertEquals("Bearer passed-key", authorization.get());
        assertTrue(body.get().contains("custom-sender@example.com"));
        assertTrue(body.get().contains("to@example.com"));
    }
}
