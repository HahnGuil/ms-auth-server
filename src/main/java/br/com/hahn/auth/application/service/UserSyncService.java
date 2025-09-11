package br.com.hahn.auth.application.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@AllArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserService userService;

    public void syncUser(String uuid, String applicationCode) {
        log.info("UserSyncService: Star syncUser");
        UUID userId = UUID.fromString(uuid);
        Long applicationId = Long.valueOf(applicationCode);

        userService.setApplicationToUser(userId, applicationId);
    }

}
