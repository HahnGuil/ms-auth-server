package br.com.hahn.auth.infrastructure.service;

import br.com.hahn.auth.application.service.UserSyncService;
import br.com.hahn.auth.domain.model.UserSyncEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class UserSyncConsumer {

    private final UserSyncService userSyncService;

    @KafkaListener(topics = "sync-application", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(UserSyncEvent event){
        log.info("UseSyncConsumer: Call UserSyncService to start syncUser");
        userSyncService.syncUser(event.getUuid(), event.getApplicationCode());
    }
}
