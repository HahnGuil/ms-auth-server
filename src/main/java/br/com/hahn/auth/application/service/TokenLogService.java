package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.InvalidTokenException;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.model.InvalidatedToken;
import br.com.hahn.auth.domain.model.TokenLog;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.TokenLogRepository;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class TokenLogService {

    private final TokenLogRepository loginLogRepository;
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
        log.info("TokenLogService: Detective token for user: {} at {}", userId, DateTimeConverter.formatInstantNow());
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
        log.info("TokenLogService: Save Token on data base, for user: {}, Scope Token is: {}, at: {}", user.getUserId(), scopeToken, DateTimeConverter.formatInstantNow());
        var loginLog = loginLogRepository.save(convertToEntity(user, scopeToken, createDate));
        if(!scopeToken.equals(ScopeToken.RECOVER_CODE)){
            log.info("TokenLogService: Scope token, is different of RECOVER: {}. Save token for user: {} on LoggedNow at: {}", scopeToken, user.getUserId(), DateTimeConverter.formatInstantNow());
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
        log.info("TokenLogService: Find active token for token log id: {}, at: {}", tokenLogId, DateTimeConverter.formatInstantNow());
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
        log.info("TokenLogServe: Find tokens log with expiration time to delete at: {}",DateTimeConverter.formatInstantNow());
        return loginLogRepository.findExpiredActiveTokens(expirationTime);
    }

    /**
     * Retrieves a TokenLog entity by its ID.
     * <p>
     * This method attempts to find a TokenLog in the database using the provided token log ID.
     * If the TokenLog is not found, it logs an error message and throws an InvalidTokenException.
     *
     * @author HahnGuil
     * @param tokenLogId The UUID of the token log to be retrieved.
     * @return The TokenLog entity if found.
     * @throws InvalidTokenException if no TokenLog is found for the given ID.
     */
    public TokenLog findById(UUID tokenLogId){
        return loginLogRepository.findById(tokenLogId).orElseThrow(() -> {
            log.error("TokenLogService: Not foud Token for user: {}. Throw InvalidTokenException at: {}", tokenLogId, DateTimeConverter.formatInstantNow());
            return new InvalidTokenException(ErrorsResponses.INVALID_TOKEN.getMessage());
        });
    }

    /**
     * Checks if a TokenLog entity exists based on its ID.
     * <p>
     * This method queries the database to determine if a TokenLog with the given ID exists.
     *
     * @author HahnGuil
     * @param tokenLogId The UUID of the token log to check for existence.
     * @return true if the TokenLog exists, false otherwise.
     */
    public boolean existsById(UUID tokenLogId){
        return loginLogRepository.existsById(tokenLogId);
    }

    /**
     * Validates if a given TokenLog is active.
     * <p>
     * This method checks whether the provided TokenLog entity is active. If the token is not active,
     * it logs an error message and throws an InvalidTokenException.
     * </p>
     *
     * @author HahnGuil
     * @param tokenLog The TokenLog entity to be validated.
     * @throws InvalidTokenException if the token is inactive or invalid.
     */
    public void isTokenLogValid(TokenLog tokenLog){
        log.info("TokenLogService: Check if the Token: {} is active: at: {}", tokenLog.getIdTokenLog(), DateTimeConverter.formatInstantNow());
        if(!tokenLog.isActiveToken()){
            log.error("TokenLogService: Token: {} is invalid or deactivate. Throw InvalidTokenException at: {}", tokenLog.getIdTokenLog(), DateTimeConverter.formatInstantNow());
            throw new InvalidTokenException(ErrorsResponses.INVALID_TOKEN.getMessage());
        }
    }

    /**
     * Validates that the provided TokenLog has a scope appropriate for login operations.
     * <p>
     * This method checks whether the token's scope is either {@code ScopeToken.LOGIN_TOKEN}
     * or {@code ScopeToken.REGISTER_TOKEN}. If the scope is different, an error is logged
     * and an {@link InvalidTokenException} is thrown.
     * </p>
     *
     * @author HahnGuil
     * @param tokenLog the TokenLog to validate; its scope is checked
     * @throws InvalidTokenException if the token scope is not LOGIN_TOKEN or REGISTER_TOKEN
     */
    public void isExpectedScopeToken(TokenLog tokenLog){
        log.info("TokenLogService: Check the scope of token: {} at: {}", tokenLog.getIdTokenLog(), DateTimeConverter.formatInstantNow());
        var scope = tokenLog.getScopeToken();
        if(!ScopeToken.LOGIN_TOKEN.equals(scope) && !ScopeToken.REGISTER_TOKEN.equals(scope)){
            log.error("TokenLogService: Token: {} has invalid scope: {}. Throw InvalidTokenException at: {}", tokenLog.getIdTokenLog(), scope, DateTimeConverter.formatInstantNow());
            throw new InvalidTokenException(ErrorsResponses.SCOPE_TOKEN_INVALID.getMessage() + tokenLog.getScopeToken().toString());
        }
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
        log.info("LoginLogService: invalidate token for User {}, with LoginLog: {}, at {}", tokenLog.getUserId(), tokenLog.getIdTokenLog(), DateTimeConverter.formatInstantNow());
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
        return loginLogRepository.findTopByUserIdOrderByCreateDateDesc(userId);
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
        log.info("TokenLogService: Convert to entity TokenLog at: {}", DateTimeConverter.formatInstantNow());
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
        log.info("TokenLogService: Convert to InvalidatedToken at: {}", DateTimeConverter.formatInstantNow());
        InvalidatedToken invalidatedToken = new InvalidatedToken();
        invalidatedToken.setUserId(userId);
        invalidatedToken.setLoginLogId(loginLogId);
        invalidatedToken.setDateInvalidate(LocalDateTime.now());
        invalidatedToken.setTypeInvalidation(typeInvalidation);
        return invalidatedToken;
    }
}
