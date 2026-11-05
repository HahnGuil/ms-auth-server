package br.com.hahn.auth.infrastructure.service;

import br.com.hahn.auth.application.execption.InvalidOperationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserDataClientTest {
    @Test void patchesWithBearerAuthorizationForBothTokenFormats() {
        List<String> auth = new ArrayList<>();
        var builder = WebClient.builder().exchangeFunction(request -> {
            auth.add(request.headers().getFirst("Authorization"));
            return reactor.core.publisher.Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build());
        });
        UserDataClient client = new UserDataClient(builder, "https://users.example");
        ReflectionTestUtils.setField(client, "patchUserEmailPath", "/users");
        client.patchUserByEmailHeader("raw-token", "user@example.com");
        client.patchUserByEmailHeader("Bearer existing-token", "user@example.com");
        assertEquals(List.of("Bearer raw-token", "Bearer existing-token"), auth);
    }

    @Test void reportsRemoteServiceError() {
        var builder = WebClient.builder().exchangeFunction(request -> reactor.core.publisher.Mono.just(
                ClientResponse.create(HttpStatus.BAD_REQUEST).body("bad request").build()));
        UserDataClient client = new UserDataClient(builder, "https://users.example");
        ReflectionTestUtils.setField(client, "patchUserEmailPath", "/users");
        assertThrows(InvalidOperationException.class, () -> client.patchUserByEmailHeader("token", "user@example.com"));
    }
}
