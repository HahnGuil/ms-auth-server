package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.response.LoginLogResponseDTO;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.model.LoginLog;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.LoginLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;

    public LoginLogService(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }

    @Transactional
    public LoginLogResponseDTO saveLoginLog(User user, ScopeToken scopeToken, LocalDateTime dateLogin) {
        LoginLog savedLog = loginLogRepository.save(convertToEntity(user, scopeToken, dateLogin));
        return new LoginLogResponseDTO(savedLog.getIdLoginLog(), savedLog.getScopeToken(), dateLogin);
    }

    private LoginLog convertToEntity(User user, ScopeToken scopeToken, LocalDateTime dateLogin) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUsers(Collections.singleton(user));
        loginLog.setScopeToken(scopeToken);
        loginLog.setDateLogin(dateLogin);
        return loginLog;
    }

}
