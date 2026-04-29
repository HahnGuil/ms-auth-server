package br.com.hahn.auth.application.service;

import br.com.hahn.auth.infrastructure.service.UserDataClient;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDataService {

    private final UserDataClient userDataClient;

    public void updateUserByEmailHeader(Jwt jwt, String userEmail) {
        log.info("UserDataService: Updating user by email header for {} at {}", userEmail, DateTimeConverter.formatInstantNow());
        userDataClient.patchUserByEmailHeader(jwt.getTokenValue(), userEmail);
    }
}

