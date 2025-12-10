package br.com.hahn.auth.infrastructure.service;

import br.com.hahn.auth.application.service.UserSyncService;
import br.com.hahn.auth.domain.model.UserSyncEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSyncConsumer {

    private final UserSyncService userSyncService;

    @KafkaListener(topics = "sync-application", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(UserSyncEvent event){
        log.info("UseSyncConsumer: Call UserSyncService to start sync user: {} to application: {}, at: {}", event.getUuid(), event.getApplicationCode(), Instant.now());
        userSyncService.syncUser(event.getUuid(), event.getApplicationCode());
    }
}
