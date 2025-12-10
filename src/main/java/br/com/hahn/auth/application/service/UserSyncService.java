package br.com.hahn.auth.application.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserService userService;

    public void syncUser(String uuid, String applicationCode) {
        log.info("UserSyncService: Star syncUser. Add application: {}, to user: {} at: {}", applicationCode, uuid, Instant.now());
        var userId = UUID.fromString(uuid);
        var applicationId = Long.valueOf(applicationCode);
        userService.setApplicationToUser(userId, applicationId);
    }

}
