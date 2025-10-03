package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.InvalidRefreshTokenException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.model.LoginLog;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.LoginLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@Slf4j
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;

    public LoginLogService(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }

    @Transactional
    public LoginLog saveLoginLog(User user, ScopeToken scopeToken, LocalDateTime dateLogin) {
        log.info("LoginLogService: Sava login log on database");
        return loginLogRepository.save(convertToEntity(user, scopeToken, dateLogin));
    }

    public LoginLog findById(UUID loginLogId){
        log.info("LoginLogServie: Find LoginLog By id");
        return loginLogRepository.findById(loginLogId).orElseThrow(() -> new InvalidRefreshTokenException("Invalid login session"));
    }

    public boolean isTokenValid (UUID loginLogId){
        return loginLogRepository.findActiveTokenByLoginLogId(loginLogId);
    }

    // TODO - Criar rotina de eventos para salvar tokens falses em outra tabela
    @Transactional
    public void invalidateToken(UUID userId){
        log.info("LoginLogService: Invalidated old token");
        loginLogRepository.deactivateActiveTokenByUserId(userId);
    }

    public boolean existsById(UUID loginLogId) {
        return loginLogRepository.existsById(loginLogId);
    }

    private LoginLog convertToEntity(User user, ScopeToken scopeToken, LocalDateTime dateLogin) {
        log.info("LoginLogService: Convert login log to entity");
        LoginLog loginLog = new LoginLog();
        loginLog.setUsers(Collections.singleton(user));
        loginLog.setScopeToken(scopeToken);
        loginLog.setDateLogin(dateLogin);
        loginLog.setActiveToken(true);
        return loginLog;
    }

}
