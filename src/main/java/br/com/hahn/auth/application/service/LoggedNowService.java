package br.com.hahn.auth.application.service;

import br.com.hahn.auth.domain.model.LoggedNow;
import br.com.hahn.auth.domain.respository.LoggedNowRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LoggedNowService {

    private LoggedNowRepository loggedNowRepository;

    @Transactional
    public void save(UUID userId, UUID loginLogId, LocalDateTime dateLogin){
        var loggedNow = convertToEntity(userId, loginLogId, dateLogin);
        loggedNowRepository.save(loggedNow);
    }

    private LoggedNow convertToEntity(UUID userId, UUID loginLogId, LocalDateTime dateLogin){
        LoggedNow loggedNow = new LoggedNow();
        loggedNow.setUserId(userId);
        loggedNow.setLoginLogId(loginLogId);
        loggedNow.setDateLogin(dateLogin);
        loggedNow.setUseRefresh(false);
        loggedNow.setDateRefresh(null);
        return loggedNow;
    }

}
