package br.com.hahn.auth.application.service;

import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.model.InvalidatedToken;
import br.com.hahn.auth.domain.model.TokenLog;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.LoginLogRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class TokenLogService {

    private final LoginLogRepository loginLogRepository;
    private final LoggedNowService loggedNowService;
    private final InvalidatedTokenService invalidatedTokenService;

    /**
     * Deactivates the active token for a user and invalidates it.
     * <p>
     * This method performs the following steps:
     * 1. Deactivates the active token associated with the user in the database.
     * 2. Retrieves the most recent login record for the user.
     * 3. Invalidates the token associated with the retrieved login record.
     *
     * @author HahnGuil
     * @param userId           The UUID of the user whose token will be deactivated.
     * @param typeInvalidation The type of invalidation to be applied to the token.
     */
    @Transactional
    public void deactivateActiveToken(UUID userId, TypeInvalidation typeInvalidation){
        log.info("TokenLogService: Detective token for user: {} at {}", userId, Instant.now());
        loginLogRepository.deactivateActiveTokenByUserId(userId);

        var tokenLog = findLoginLogByUserId(userId);

        log.info("LoginLogServe: Call Invalidate old token method for {}", tokenLog.getIdTokenLog());
        invalidateToken(tokenLog, typeInvalidation);
    }

    /**
     * Saves a new token log for a user in the database.
     * <p>
     * This method performs the following steps:
     * 1. Converts the provided user, scope token, and creation date into a TokenLog entity.
     * 2. Saves the TokenLog entity in the database.
     * 3. If the scope token is not RECOVER_CODE, it saves the token in the LoggedNow service.
     *
     * @author HahnGuil
     * @param user       The user for whom the token log is being saved.
     * @param scopeToken The scope of the token being saved.
     * @param createDate The creation date of the token log.
     * @return The saved TokenLog entity.
     */
    @Transactional
    public TokenLog saveTokenLog(User user, ScopeToken scopeToken, LocalDateTime createDate) {
        log.info("TokenLogService: Save Token on data base, for user: {}, Scope Token is: {}, at: {}", user.getUserId(), scopeToken, Instant.now());
        var loginLog = loginLogRepository.save(convertToEntity(user, scopeToken, createDate));
        if(!scopeToken.equals(ScopeToken.RECOVER_CODE)){
            log.info("TokenLogService: Scope token, is different of RECOVER: {}. Save token for user: {} on LoggedNow at: {}", scopeToken, user.getUserId(), Instant.now());
            loggedNowService.save(user.getUserId(), loginLog.getIdTokenLog(), createDate);
        }
        return loginLog;
    }

    /**
     * Checks if a token is valid based on its ID.
     * <p>
     * This method queries the database to determine if there is an active token
     * associated with the provided token log ID.
     *
     * @author HahnGuil
     * @param tokenLogId The UUID of the token log to be validated.
     * @return true if the token is active, false otherwise.
     */
    public boolean isTokenValid (UUID tokenLogId){
        log.info("TokenLogService: Find active token for token log id: {}, at: {}", tokenLogId, Instant.now());
        return loginLogRepository.findActiveTokenByLoginLogId(tokenLogId);
    }

    /**
     * Retrieves a list of expired active tokens based on the provided expiration time.
     * <p>
     * This method queries the database to find all active tokens that have expired
     * before the specified expiration time.
     *
     * @author HahnGuil
     * @param expirationTime The cutoff time to determine which tokens are considered expired.
     * @return A list of TokenLog entities representing the expired active tokens.
     */
    public List<TokenLog> findExpiredActiveTokens(LocalDateTime expirationTime) {
        log.info("TokenLogServe: Find tokens log with expiration time to delete at: {}",Instant.now());
        return loginLogRepository.findExpiredActiveTokens(expirationTime);
    }

    /**
     * Invalidates a token for a user based on the provided token log and invalidation type.
     * <p>
     * This method performs the following steps:
     * 1. Logs the invalidation process with details about the user and token log.
     * 2. Converts the token log into an InvalidatedToken entity.
     * 3. Saves the InvalidatedToken entity using the InvalidatedTokenService.
     *
     * @author HahnGuil
     * @param tokenLog        The TokenLog entity containing details of the token to be invalidated.
     * @param typeInvalidation The type of invalidation to be applied to the token.
     */
    private void invalidateToken(TokenLog tokenLog, TypeInvalidation typeInvalidation){
        log.info("LoginLogService: invalidate token for User {}, with LoginLog: {}, at {}", tokenLog.getUserId(), tokenLog.getIdTokenLog(), Instant.now());
        var invalidateToken = convertToInvalidatedTokenEntity(tokenLog.getUserId(), tokenLog.getIdTokenLog(), typeInvalidation);
        invalidatedTokenService.save(invalidateToken);
    }

    /**
     * Retrieves the most recent login log for a user based on their user ID.
     * <p>
     * This method queries the database to find the latest login record
     * for the specified user, ordered by the login date in descending order.
     *
     * @author HahnGuil
     * @param userId The UUID of the user whose most recent login log is to be retrieved.
     * @return The most recent TokenLog entity for the user.
     */
    private TokenLog findLoginLogByUserId(UUID userId){
        return loginLogRepository.findTopByUserIdOrderByDateLoginDesc(userId);
    }

    /**
     * Converts the provided user, scope token, and creation date into a TokenLog entity.
     * <p>
     * This method initializes a new TokenLog object, sets its properties based on the
     * provided parameters, and marks the token as active.
     *
     * @author HahnGuil
     * @param user       The user for whom the token log is being created.
     * @param scopeToken The scope of the token being created.
     * @param createDate The creation date of the token log.
     * @return A TokenLog entity populated with the provided data.
     */
    private TokenLog convertToEntity(User user, ScopeToken scopeToken, LocalDateTime createDate) {
        log.info("TokenLogService: Convert to entity TokenLog at: {}", Instant.now());
        TokenLog tokenLog = new TokenLog();
        tokenLog.setUserId(user.getUserId());
        tokenLog.setScopeToken(scopeToken);
        tokenLog.setCreateDate(createDate);
        tokenLog.setActiveToken(true);
        return tokenLog;
    }

    /**
     * Converts the provided user ID, login log ID, and invalidation type into an InvalidatedToken entity.
     * <p>
     * This method initializes a new InvalidatedToken object, sets its properties based on the
     * provided parameters, and assigns the current date and time as the invalidation date.
     *
     * @author HahnGuil
     * @param userId           The UUID of the user associated with the invalidated token.
     * @param loginLogId       The UUID of the login log associated with the invalidated token.
     * @param typeInvalidation The type of invalidation to be applied to the token.
     * @return An InvalidatedToken entity populated with the provided data.
     */
    private InvalidatedToken convertToInvalidatedTokenEntity(UUID userId, UUID loginLogId, TypeInvalidation typeInvalidation){
        log.info("TokenLogService: Convert to InvalidatedToken at: {}", Instant.now());
        InvalidatedToken invalidatedToken = new InvalidatedToken();
        invalidatedToken.setUserId(userId);
        invalidatedToken.setLoginLogId(loginLogId);
        invalidatedToken.setDateInvalidate(LocalDateTime.now());
        invalidatedToken.setTypeInvalidation(typeInvalidation);
        return invalidatedToken;
    }
}
