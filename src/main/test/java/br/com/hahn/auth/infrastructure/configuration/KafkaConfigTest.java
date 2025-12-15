package br.com.hahn.auth.infrastructure.configuration;

import br.com.hahn.auth.domain.model.UserSyncEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class KafkaConfigTest {

    @Mock
    private ConsumerFactory<String, UserSyncEvent> mockConsumerFactory;

    private KafkaConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "test-group");
        ReflectionTestUtils.setField(kafkaConfig, "autoOffsetReset", "earliest");
    }

    @Test
    void consumerFactoryShouldCreateWithValidProperties() {
        ConsumerFactory<String, UserSyncEvent> consumerFactory = kafkaConfig.consumerFactory();

        assertNotNull(consumerFactory);
    }

    @Test
    void kafkaListenerContainerFactoryShouldCreateWithValidConsumerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserSyncEvent> factory =
                kafkaConfig.kafkaListenerContainerFactory(mockConsumerFactory);

        assertNotNull(factory);
        assertEquals(mockConsumerFactory, factory.getConsumerFactory());
    }
}