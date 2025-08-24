package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.response.LoginLogResponseDTO;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.model.LoginLog;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.LoginLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class LoginLogService {

    private static final Logger logger = LoggerFactory.getLogger(LinkageError.class);

    private final LoginLogRepository loginLogRepository;

    public LoginLogService(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }

    @Transactional
    public LoginLogResponseDTO saveLoginLog(User user, ScopeToken scopeToken, LocalDateTime dateLogin) {
        logger.info("LoginLogService: Sava login log on database");
        LoginLog savedLog = loginLogRepository.save(convertToEntity(user, scopeToken, dateLogin));
        return new LoginLogResponseDTO(savedLog.getIdLoginLog(), savedLog.getScopeToken(), dateLogin);
    }

    private LoginLog convertToEntity(User user, ScopeToken scopeToken, LocalDateTime dateLogin) {
        logger.info("LoginLogService: Convert login log to entity");
        LoginLog loginLog = new LoginLog();
        loginLog.setUsers(Collections.singleton(user));
        loginLog.setScopeToken(scopeToken);
        loginLog.setDateLogin(dateLogin);
        return loginLog;
    }

}
