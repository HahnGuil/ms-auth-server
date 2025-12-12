package br.com.hahn.auth.application.service;

import br.com.hahn.auth.domain.model.LoggedNow;
import br.com.hahn.auth.domain.respository.LoggedNowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoggedNowService {

    private final LoggedNowRepository loggedNowRepository;

    /**
     * Retrieves a list of LoggedNow entities associated with the given user ID.
     *
     * @author HahnGuil
     * @param userId the unique identifier of the user whose LoggedNow records are to be retrieved
     * @return a list of LoggedNow entities associated with the specified user ID
     */
    public List<LoggedNow> findByUserId(UUID userId){
        log.info("LoggedNowServe: Find LoggedNow for user: {} at: {}", userId, Instant.now());
        return loggedNowRepository.findByUserId(userId);
    }

    /**
     * Persists a new LoggedNow entity in the database for the specified user.
     *
     * @author HahnGuil
     * @param userId the unique identifier of the user for whom the LoggedNow record is being saved
     * @param tokenLogId the unique identifier of the token log associated with the login event
     * @param dateLogin the date and time of the login event
     */
    @Transactional
    public void save(UUID userId, UUID tokenLogId, LocalDateTime dateLogin){
        log.info("LoggedNowService: Save LoggedNow to user: {}, with token id: {}, at: {}", userId, tokenLogId, Instant.now());
        var loggedNow = convertToEntity(userId, tokenLogId, dateLogin);
        loggedNowRepository.save(loggedNow);
    }

    /**
     * Deletes all LoggedNow entities associated with the specified user ID.
     *
     * @author HahnGuil
     * @param userId the unique identifier of the user whose LoggedNow records are to be deleted
     */
    @Transactional
    public void deleteByUserId(UUID userId){
        log.info("LoggedNowService: Delete LoggedNow for user: {}, at: {}", userId, Instant.now());
        loggedNowRepository.deleteByUserId(userId);
    }

    /**
     * Converts the provided user ID, token log ID, and login date into a LoggedNow entity.
     *
     * @author HahnGuil
     * @param userId the unique identifier of the user
     * @param tokenLogId the unique identifier of the token log associated with the login event
     * @param dateLogin the date and time of the login event
     * @return a LoggedNow entity populated with the provided data
     */
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
