package br.com.hahn.auth.application.service;

import br.com.hahn.auth.util.DateTimeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserService userService;

    public void syncUser(String uuid, String applicationCode) {
        log.info("UserSyncService: Star syncUser. Add application: {}, to user: {} at: {}", applicationCode, uuid, DateTimeConverter.formatInstantNow());
        var userId = UUID.fromString(uuid);
        var applicationId = Long.valueOf(applicationCode);
        userService.setApplicationToUser(userId, applicationId);
    }

}
