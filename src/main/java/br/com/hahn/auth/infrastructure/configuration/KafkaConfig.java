package br.com.hahn.auth.infrastructure.configuration;

import br.com.hahn.auth.domain.model.UserSyncEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, UserSyncEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "update-user-data");
        props.put("key.deserializer", StringDeserializer.class);
        props.put("value.deserializer", JsonDeserializer.class);
        props.put("spring.kafka.consumer.auto-offset-reset", "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "br.com.hahn.auth.application.dto, br.com.hahn.auth.domain.model");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(UserSyncEvent.class, false)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserSyncEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, UserSyncEvent> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, UserSyncEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
