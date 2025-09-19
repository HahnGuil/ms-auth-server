package br.com.hahn.auth.infrastructure.configuration;

import br.com.hahn.auth.domain.model.UserSyncEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;



    @Bean
    public ConsumerFactory<String, UserSyncEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", groupId);
        props.put("key.deserializer", StringDeserializer.class);
        props.put("value.deserializer", JsonDeserializer.class);
        props.put("auto.offset.reset", autoOffsetReset);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "br.com.hahn.auth.application.dto,br.com.hahn.auth.domain.model");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "br.com.hahn.auth.domain.model.UserSyncEvent");

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
