package br.com.hahn.auth.application.service;

import br.com.hahn.auth.domain.model.LoggedNow;
import br.com.hahn.auth.domain.respository.LoggedNowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoggedNowService {

    private final LoggedNowRepository loggedNowRepository;

    public boolean existsByUserId(UUID userId){
        log.info("LoggedNowService: Find LoggedNow to user: {}, at: {}", userId, Instant.now());
        return loggedNowRepository.existsByUserId(userId);
    }

    @Transactional
    public void save(UUID userId, UUID tokenLogId, LocalDateTime dateLogin){
        log.info("LoggedNowService: Save LoggedNow to user: {}, with token id: {}, at: {}", userId, tokenLogId, Instant.now());
        var loggedNow = convertToEntity(userId, tokenLogId, dateLogin);
        loggedNowRepository.save(loggedNow);
    }

    @Transactional
    public void deleteByUserId(UUID userId){
        log.info("LoggedNowService: Delete LoggedNow for user: {}, at: {}", userId, Instant.now());
        loggedNowRepository.deleteByUserId(userId);
    }

    private LoggedNow convertToEntity(UUID userId, UUID tokenLogId, LocalDateTime dateLogin){
        log.info("LoggedNowService: Convert to LoggedNow Entity, for user: {}, with token id: {} at: {}", userId, tokenLogId, Instant.now());
        LoggedNow loggedNow = new LoggedNow();
        loggedNow.setUserId(userId);
        loggedNow.setTokenLogId(tokenLogId);
        loggedNow.setDateLogin(dateLogin);
        loggedNow.setUseRefresh(false);
        loggedNow.setDateRefresh(null);
        return loggedNow;
    }

}
