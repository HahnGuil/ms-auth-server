package br.com.hahn.auth.infrastructure.service;

import br.com.hahn.auth.domain.model.UserUpdateEvent;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for producing user update events to Kafka.
 *
 * @author HahnGuil
 * <p>This service is responsible for sending user update notifications
 * to the "user-update" Kafka topic, which can be consumed by other
 * applications that need to be notified about user changes.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserUpdateProducer {

    private final KafkaTemplate<String, UserUpdateEvent> kafkaTemplate;

    private static final String TOPIC_NAME = "user-update";

    /**
     * Sends a user update event to the Kafka topic.
     *
     * <p>This method publishes a {@link UserUpdateEvent} containing the user's email
     * and the application UUID to the "user-update" topic. Logging is performed to
     * track the event publication.</p>
     *
     * @author HahnGuil
     * @param userUpdateEvent the {@link UserUpdateEvent} containing the user email and application UUID
     */
    public void sendUserUpdateEvent(UserUpdateEvent userUpdateEvent) {
        log.info("UserUpdateProducer: Sending user update event for email: {}, applicationUuid: {}, at: {}",
                userUpdateEvent.getEmail(), userUpdateEvent.getApplicationName(),
                DateTimeConverter.formatInstantNow());

        kafkaTemplate.send(TOPIC_NAME, userUpdateEvent);

        log.info("UserUpdateProducer: User update event sent successfully for email: {}, at: {}",
                userUpdateEvent.getEmail(), DateTimeConverter.formatInstantNow());
    }
}

