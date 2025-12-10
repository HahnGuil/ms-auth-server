package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.InvalidRefreshTokenException;
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

import java.nio.file.LinkOption;
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

//    Refatoração -----------------

    @Transactional
    public void deactivateActiveToken(UUID userId, TypeInvalidation typeInvalidation){
        log.info("LoginLogService: Desactive token for user: {} at {}", userId, Instant.now());
        loginLogRepository.deactivateActiveTokenByUserId(userId);

        var tokenLog = this.findLoginLogByUserId(userId);

        log.info("LoginLogServe: Call Invalidate old token method for {}", tokenLog.getIdLoginLog());
        invalidateToken(tokenLog, typeInvalidation);
    }

    private void invalidateToken(TokenLog tokenLog, TypeInvalidation typeInvalidation){
        log.info("LoginLogService: invalidate token for User {}, with LoginLog: {}, at {}", tokenLog.getUserId(), tokenLog.getIdLoginLog(), Instant.now());
        var invalidateToken = convertToInvalidatedTokenEntity(tokenLog.getUserId(), tokenLog.getIdLoginLog(), typeInvalidation);
        invalidatedTokenService.save(invalidateToken);
    }

    @Transactional
    public TokenLog saveTokenLog(User user, ScopeToken scopeToken, LocalDateTime createDate) {
        log.info("TokenLogService: Save Token on data base, for user: {}, Scope Tokne is: {}, at: {}", user.getUserId(), scopeToken, Instant.now());
        var loginLog = loginLogRepository.save(convertToEntity(user, scopeToken, createDate));
        if(!scopeToken.equals(ScopeToken.RECOVER_CODE)){
            log.info("TokenLogService: Scope token, is different of RECOVER: {}. Save token for user: {} on LoggedNow at: {}", scopeToken, user.getUserId(), Instant.now());
            loggedNowService.save(user.getUserId(), loginLog.getIdLoginLog(), createDate);
        }
        return loginLog;
    }

//    Código antigo -------------


    public TokenLog findById(UUID loginLogId){
        log.info("LoginLogServie: Find LoginLog By id");
        return loginLogRepository.findById(loginLogId).orElseThrow(() -> new InvalidRefreshTokenException("Invalid login session"));
    }

    public boolean isTokenValid (UUID loginLogId){
        return loginLogRepository.findActiveTokenByLoginLogId(loginLogId);
    }



    public boolean existsById(UUID loginLogId) {
        return loginLogRepository.existsById(loginLogId);
    }

    public List<TokenLog> findExpiredActiveTokens(LocalDateTime expirationTime) {
        log.info("LoginLogService: find expired active tokens");
        return loginLogRepository.findExpiredActiveTokens(expirationTime);
    }



    private TokenLog findLoginLogByUserId(UUID userId){
        return loginLogRepository.findTopByUserIdOrderByDateLoginDesc(userId);
    }

    private TokenLog convertToEntity(User user, ScopeToken scopeToken, LocalDateTime createDate) {
        log.info("LoginLogService: Convert login log to entity");
        TokenLog tokenLog = new TokenLog();
        tokenLog.setUserId(user.getUserId());
        tokenLog.setScopeToken(scopeToken);
        tokenLog.setCreateDate(createDate);
        tokenLog.setActiveToken(true);
        return tokenLog;
    }

    private InvalidatedToken convertToInvalidatedTokenEntity(UUID userId, UUID loginLogId, TypeInvalidation typeInvalidation){
        InvalidatedToken invalidatedToken = new InvalidatedToken();
        invalidatedToken.setUserId(userId);
        invalidatedToken.setLoginLogId(loginLogId);
        invalidatedToken.setDateInvalidate(LocalDateTime.now());
        invalidatedToken.setTypeInvalidation(typeInvalidation);
        return invalidatedToken;
    }
}
