package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.ApplicationNotFoundException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.model.*;
import br.com.hahn.auth.domain.respository.ApplicationRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final TokenLogService tokenLogService;
    private final TokenService tokenService;

    /**
     * Finds an application by its ID.
     * <p>
     * This method logs an informational message when starting the lookup and an error
     * message if the application is not found. If the application cannot be found,
     * an {@link ApplicationNotFoundException} is thrown.
     *
     * @author HahnGuil
     * @param id the ID of the application to find
     * @return the Application matching the provided ID
     * @throws ApplicationNotFoundException if the application is not found
     */
    public Application findById(Long id) {
        log.info("ApplicationService: Find application: {} at: {}", id, Instant.now());
        return applicationRepository.findById(id).orElseThrow(() -> {
            log.error("ApplicationService: Application not found for id: {}. Throw the ApplicationNotFoundException at: {}", id, DateTimeConverter.formatInstantNow());
            return new ApplicationNotFoundException("Application not found");
        });
    }

    public ApplicationRegisterResponse registerApplication(UUID publicId){
        var application = findByPublicId(publicId);

        var applicationToken = tokenService.generateApplicationToken(application, generateTokenLog(application, ScopeToken.APPLICATION_TOKEN));
        var refreshApplicationToken = tokenService.generateApplicationRefreshToken(application, generateTokenLog(application, ScopeToken.APPLICATION_REFRESH_TOKEN));

        ApplicationRegisterResponse response = new ApplicationRegisterResponse();
        response.setApplicationToken(applicationToken);
        response.setRefreshApplicationToken(refreshApplicationToken);
        return response;
    }

    private Application findByPublicId(UUID publicId){
        log.info("ApplicationService: Find application for publicId: {} at: {}", publicId, DateTimeConverter.formatInstantNow());

        return applicationRepository.findApplicationByPublicId(publicId).orElseThrow(() -> {
            log.error("ApplicationService: Application not found for public id: {}. Throw the ApplicationNotFoundException at: {}", publicId, DateTimeConverter.formatInstantNow());
            return new ApplicationNotFoundException("Application not found");
        });
    }

    public void isUserRegisterOnApplication(UUID applicationPublicId, User user){
        log.info(
                "ApplicationService: Validate if user: {} is registered on application publicId: {} at: {}",
                user != null ? user.getFirstName() : null,
                applicationPublicId,
                DateTimeConverter.formatInstantNow()
        );

        if (applicationPublicId == null || user == null || user.getApplications() == null || user.getApplications().isEmpty()) {
            throw new ApplicationNotFoundException("User is not registered on application");
        }

        boolean isRegistered = user.getApplications().stream()
                .map(Application::getPublicId)
                .anyMatch(applicationPublicId::equals);

        if (!isRegistered) {
            throw new ApplicationNotFoundException("User is not registered on application");
        }
    }

    private TokenLog generateTokenLog(Application application, ScopeToken scopeToken){
        return tokenLogService.saveApplicationTokenLog(application, scopeToken, LocalDateTime.now());
    }
}
