package br.com.hahn.auth.infrastructure.configuration;

import br.com.hahn.auth.domain.model.UserSyncEvent;
import br.com.hahn.auth.domain.model.UserUpdateEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

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

    /**
     * Creates a Kafka ConsumerFactory bean for deserializing messages with String keys
     * and UserSyncEvent values.
     * <p>
     * This method configures the necessary properties for the Kafka consumer, such as
     * bootstrap servers, group ID, deserializers, and trusted packages for JSON deserialization.
     * It returns a DefaultKafkaConsumerFactory instance configured with these properties.
     *
     * @author HahnGuil
     * @return ConsumerFactory<String, UserSyncEvent> the configured Kafka ConsumerFactory
     */
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

    /**
     * Creates a Kafka Listener Container Factory bean for processing messages with String keys
     * and UserSyncEvent values.
     * <p>
     * This method configures a ConcurrentKafkaListenerContainerFactory using the provided
     * ConsumerFactory. The factory is used to create Kafka listener containers that handle
     * message consumption.
     *
     * @author HahnGuil
     * @param consumerFactory the ConsumerFactory used to configure the listener container factory
     * @return ConcurrentKafkaListenerContainerFactory<String, UserSyncEvent> the configured Kafka Listener Container Factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserSyncEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, UserSyncEvent> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, UserSyncEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        return factory;
    }

    /**
     * Creates a Kafka ProducerFactory bean for serializing messages with String keys
     * and UserUpdateEvent values.
     * <p>
     * This method configures the necessary properties for the Kafka producer, such as
     * bootstrap servers and serializers for JSON serialization.
     * It returns a DefaultKafkaProducerFactory instance configured with these properties.
     *
     * @author HahnGuil
     * @return ProducerFactory<String, UserUpdateEvent> the configured Kafka ProducerFactory
     */
    @Bean
    public ProducerFactory<String, UserUpdateEvent> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", StringSerializer.class);
        props.put("value.serializer", JsonSerializer.class);
        props.put("acks", "all");

        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * Creates a Kafka Template bean for sending messages with String keys
     * and UserUpdateEvent values.
     * <p>
     * This method uses the provided ProducerFactory to create a KafkaTemplate instance
     * that can be used to send messages to Kafka topics.
     *
     * @author HahnGuil
     * @param producerFactory the ProducerFactory used to create the KafkaTemplate
     * @return KafkaTemplate<String, UserUpdateEvent> the configured Kafka Template
     */
    @Bean
    public KafkaTemplate<String, UserUpdateEvent> kafkaTemplate(ProducerFactory<String, UserUpdateEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}