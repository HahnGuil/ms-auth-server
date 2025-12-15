package br.com.hahn.auth.infrastructure.service;

import br.com.hahn.auth.application.service.UserSyncService;
import br.com.hahn.auth.domain.model.UserSyncEvent;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSyncConsumer {

    private final UserSyncService userSyncService;

    /**
     * Consumes a Kafka message containing user synchronization data.
     *
     * <p>This method listens to the "sync-application" topic and processes incoming
     * {@link UserSyncEvent} messages. It logs the synchronization details and delegates
     * the synchronization process to the {@link UserSyncService}.</p>
     *
     * @author HahnGuil
     * @param event the {@link UserSyncEvent} containing user and application synchronization details
     */
    @KafkaListener(topics = "sync-application", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(UserSyncEvent event){
        log.info("UseSyncConsumer: Call UserSyncService to start sync user: {} to application: {}, at: {}", event.getUuid(), event.getApplicationCode(), DateTimeConverter.formatInstantNow());
        userSyncService.syncUser(event.getUuid(), event.getApplicationCode());
    }
}