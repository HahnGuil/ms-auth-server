package br.com.hahn.auth.application.service;

import br.com.hahn.auth.domain.model.LoggedNow;
import br.com.hahn.auth.domain.respository.LoggedNowRepository;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoggedNowService {

    private final LoggedNowRepository loggedNowRepository;

    /**
     * Finds the active sessions of a user in one application (null for sessions without application).
     */
    public List<LoggedNow> findByUserIdAndApplication(UUID userId, UUID applicationPublicId){
        log.info("LoggedNowServe: Find LoggedNow for user: {} and application: {} at: {}", userId, applicationPublicId, DateTimeConverter.formatInstantNow());
        return applicationPublicId == null
                ? loggedNowRepository.findByUserIdAndApplicationPublicIdIsNull(userId)
                : loggedNowRepository.findByUserIdAndApplicationPublicId(userId, applicationPublicId);
    }

    @Transactional
    public void save(UUID userId, UUID applicationPublicId, UUID tokenLogId, LocalDateTime dateLogin){
        log.info("LoggedNowService: Save LoggedNow to user: {}, application: {}, with token id: {}, at: {}", userId, applicationPublicId, tokenLogId, DateTimeConverter.formatInstantNow());
        var loggedNow = convertToEntity(userId, applicationPublicId, tokenLogId, dateLogin);
        loggedNowRepository.save(loggedNow);
    }

    @Transactional
    public void deleteByUserIdAndApplication(UUID userId, UUID applicationPublicId){
        log.info("LoggedNowService: Delete LoggedNow for user: {} and application: {}, at: {}", userId, applicationPublicId, DateTimeConverter.formatInstantNow());
        if(applicationPublicId == null){
            loggedNowRepository.deleteByUserIdWithoutApplication(userId);
        } else {
            loggedNowRepository.deleteByUserIdAndApplicationPublicId(userId, applicationPublicId);
        }
    }

    private LoggedNow convertToEntity(UUID userId, UUID applicationPublicId, UUID tokenLogId, LocalDateTime dateLogin){
        log.info("LoggedNowService: Convert to LoggedNow Entity, for user: {}, with token id: {} at: {}", userId, tokenLogId, DateTimeConverter.formatInstantNow());
        LoggedNow loggedNow = new LoggedNow();
        loggedNow.setUserId(userId);
        loggedNow.setApplicationPublicId(applicationPublicId);
        loggedNow.setTokenLogId(tokenLogId);
        loggedNow.setDateLogin(dateLogin);
        loggedNow.setUseRefresh(false);
        loggedNow.setDateRefresh(null);
        return loggedNow;
    }
}