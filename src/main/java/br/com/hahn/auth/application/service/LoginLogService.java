package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.InvalidRefreshTokenException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.model.InvalidatedToken;
import br.com.hahn.auth.domain.model.LoginLog;
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
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;
    private final LoggedNowService loggedNowService;
    private final InvalidatedTokenService invalidatedTokenService;

//    Refatoração -----------------

    @Transactional
    public void deactivateActiveToken(UUID userId, TypeInvalidation typeInvalidation){
        log.info("LoginLogService: Desactive token for user: {} at {}", userId, Instant.now());
        loginLogRepository.deactivateActiveTokenByUserId(userId);

        var loginLog = this.findLoginLogByUserId(userId);

        log.info("LoginLogServe: Call Invalidate old token method for {}", loginLog.getIdLoginLog());
        invalidateToken(loginLog, typeInvalidation);
    }

    private void invalidateToken(LoginLog loginLog, TypeInvalidation typeInvalidation){
        log.info("LoginLogService: invalidate token for User {}, with LoginLog: {}, at {}", loginLog.getUserId(), loginLog.getIdLoginLog(), Instant.now());
        var invalidateToken = convertToInvalidatedTokenEntity(loginLog.getUserId(), loginLog.getIdLoginLog(), typeInvalidation);
        invalidatedTokenService.save(invalidateToken);
    }




//    Código antigo -------------
    @Transactional
    public LoginLog saveLoginLog(User user, ScopeToken scopeToken, LocalDateTime dateLogin) {
        log.info("LoginLogService: Sava login log on database");
        var loginLog = loginLogRepository.save(convertToEntity(user, scopeToken, dateLogin));
        loggedNowService.save(user.getUserId(), loginLog.getIdLoginLog(), dateLogin);
        return loginLog;
    }

    public LoginLog findById(UUID loginLogId){
        log.info("LoginLogServie: Find LoginLog By id");
        return loginLogRepository.findById(loginLogId).orElseThrow(() -> new InvalidRefreshTokenException("Invalid login session"));
    }

    public boolean isTokenValid (UUID loginLogId){
        return loginLogRepository.findActiveTokenByLoginLogId(loginLogId);
    }



    public boolean existsById(UUID loginLogId) {
        return loginLogRepository.existsById(loginLogId);
    }

    public List<LoginLog> findExpiredActiveTokens(LocalDateTime expirationTime) {
        log.info("LoginLogService: find expired active tokens");
        return loginLogRepository.findExpiredActiveTokens(expirationTime);
    }



    private LoginLog findLoginLogByUserId(UUID userId){
        return loginLogRepository.findTopByUserIdOrderByDateLoginDesc(userId);
    }

    private LoginLog convertToEntity(User user, ScopeToken scopeToken, LocalDateTime dateLogin) {
        log.info("LoginLogService: Convert login log to entity");
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(user.getUserId());
        loginLog.setScopeToken(scopeToken);
        loginLog.setDateLogin(dateLogin);
        loginLog.setActiveToken(true);
        return loginLog;
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
