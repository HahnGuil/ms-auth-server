package br.com.hahn.auth.infrastructure.configuration;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.KafkaFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomKafkaHealthIndicatorTest {

    @Mock
    private AdminClient mockAdminClient;

    @Mock
    private ListTopicsResult mockListTopicsResult;

    @Mock
    private KafkaFuture<Set<String>> mockKafkaFuture;

    private CustomKafkaHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new CustomKafkaHealthIndicator();
        ReflectionTestUtils.setField(healthIndicator, "bootstrapServers", "localhost:9092");
    }

    @Test
    void shouldReturnUpWhenKafkaIsAvailable() throws Exception {
        try (MockedStatic<AdminClient> mockedAdminClient = mockStatic(AdminClient.class)) {
            mockedAdminClient.when(() -> AdminClient.create(any(Properties.class))).thenReturn(mockAdminClient);
            when(mockAdminClient.listTopics()).thenReturn(mockListTopicsResult);
            when(mockListTopicsResult.names()).thenReturn(mockKafkaFuture);
            when(mockKafkaFuture.get()).thenReturn(Set.of("topic1", "topic2"));

            Health health = healthIndicator.health();

            assertEquals(Status.UP, health.getStatus());
            assertEquals("Available", health.getDetails().get("Kafka"));
        }
    }

    @Test
    void shouldReturnDownWhenKafkaIsInterrupted() throws Exception {
        try (MockedStatic<AdminClient> mockedAdminClient = mockStatic(AdminClient.class)) {
            mockedAdminClient.when(() -> AdminClient.create(any(Properties.class))).thenReturn(mockAdminClient);
            when(mockAdminClient.listTopics()).thenReturn(mockListTopicsResult);
            when(mockListTopicsResult.names()).thenReturn(mockKafkaFuture);
            when(mockKafkaFuture.get()).thenThrow(new InterruptedException());

            Health health = healthIndicator.health();

            assertEquals(Status.DOWN, health.getStatus());
            assertEquals("Interrupted", health.getDetails().get("Kafka"));
        }
    }

    @Test
    void shouldReturnDownWhenKafkaIsNotAvailable() throws Exception {
        try (MockedStatic<AdminClient> mockedAdminClient = mockStatic(AdminClient.class)) {
            mockedAdminClient.when(() -> AdminClient.create(any(Properties.class))).thenReturn(mockAdminClient);
            when(mockAdminClient.listTopics()).thenReturn(mockListTopicsResult);
            when(mockListTopicsResult.names()).thenReturn(mockKafkaFuture);
            when(mockKafkaFuture.get()).thenThrow(new ExecutionException(new RuntimeException()));

            Health health = healthIndicator.health();

            assertEquals(Status.DOWN, health.getStatus());
            assertEquals("Not available", health.getDetails().get("Kafka"));
        }
    }
}