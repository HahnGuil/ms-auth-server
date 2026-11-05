package br.com.hahn.auth.application.service;

import java.time.ZoneId;
import br.com.hahn.auth.application.execption.ApplicationNotFoundException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.model.*;
import br.com.hahn.auth.application.execption.InvalidFormatTypeException;
import br.com.hahn.auth.application.execption.ResourceAlreadyExistException;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import br.com.hahn.auth.domain.respository.ApplicationRepository;
import br.com.hahn.auth.domain.respository.PasswordPolicyRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PasswordPolicyRepository passwordPolicyRepository;

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

    /**
     * Creates a new application together with its password policy.
     * - definePasswordPolicy = false: no policy is applied (not even the default one).
     * - definePasswordPolicy = true and useDefaultPasswordPolicy = true: default policy (90 days).
     * - definePasswordPolicy = true and useDefaultPasswordPolicy = false: expires after passwordExpirationMonths.
     *
     * @author HahnGuil
     * @param request the application data
     * @return the created application
     */
    @Transactional
    public ApplicationResponse createApplication(ApplicationCreateRequest request){
        log.info("ApplicationService: Create application: {} at: {}", request.getNameApplication(), DateTimeConverter.formatInstantNow());

        var name = trimToNull(request.getNameApplication());
        var domain = trimToNull(request.getApplicationDomain());
        var email = trimToNull(request.getApplicationEmail());
        var resendApiKey = trimToNull(request.getResendApiKey());
        if(name == null || domain == null || email == null || resendApiKey == null){
            throw new InvalidFormatTypeException(ErrorsResponses.INVALID_APPLICATION_DATA.getMessage());
        }

        var policy = buildPasswordPolicy(request);

        if(applicationRepository.existsByNameApplicationIgnoreCaseOrApplicationDomainIgnoreCaseOrApplicationEmailIgnoreCase(name, domain, email)){
            log.error("ApplicationService: Application already registered. Throw ResourceAlreadyExistException at: {}", DateTimeConverter.formatInstantNow());
            throw new ResourceAlreadyExistException(ErrorsResponses.APPLICATION_ALREADY_REGISTER_ERROR.getMessage());
        }

        var application = new Application();
        application.setNameApplication(name);
        application.setApplicationDomain(domain);
        application.setApplicationEmail(email);
        application.setResendApiKey(resendApiKey);
        application = applicationRepository.save(application);

        policy.setApplication(application);
        passwordPolicyRepository.save(policy);

        return convertToApplicationResponse(application, policy);
    }

    private PasswordPolicy buildPasswordPolicy(ApplicationCreateRequest request){
        var policy = new PasswordPolicy();
        var define = Boolean.TRUE.equals(request.getDefinePasswordPolicy());
        var useDefault = Boolean.TRUE.equals(request.getUseDefaultPasswordPolicy());

        policy.setExpirationEnabled(define);
        policy.setDefaultPolicy(define && useDefault);

        if(define && !useDefault){
            var months = request.getPasswordExpirationMonths();
            if(months == null || months < 1){
                throw new InvalidFormatTypeException(ErrorsResponses.INVALID_PASSWORD_POLICY.getMessage());
            }
            policy.setExpirationMonths(months);
        }
        return policy;
    }

    private ApplicationResponse convertToApplicationResponse(Application application, PasswordPolicy policy){
        var response = new ApplicationResponse();
        response.setApplicationPublicId(application.getPublicId());
        response.setNameApplication(application.getNameApplication());
        response.setApplicationEmail(application.getApplicationEmail());
        response.setApplicationDomain(application.getApplicationDomain());
        response.setDefinePasswordPolicy(policy.isExpirationEnabled());
        response.setUseDefaultPasswordPolicy(policy.isDefaultPolicy());
        response.setPasswordExpirationMonths(policy.getExpirationMonths());
        return response;
    }

    private String trimToNull(String value){
        return value == null || value.isBlank() ? null : value.trim();
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

    public Application findByPublicId(UUID publicId){
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
        return tokenLogService.saveApplicationTokenLog(application, scopeToken, LocalDateTime.now(ZoneId.systemDefault()));
    }
}
