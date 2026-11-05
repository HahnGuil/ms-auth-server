package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.ApplicationApi;
import br.com.hahn.auth.application.service.ApplicationService;
import br.com.hahn.auth.application.service.UserService;
import br.com.hahn.auth.domain.model.ApplicationCreateRequest;
import br.com.hahn.auth.domain.model.ApplicationRegisterResponse;
import br.com.hahn.auth.domain.model.ApplicationResponse;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@Slf4j
public class ApplicationController extends AbstractController implements ApplicationApi {

    private final ApplicationService applicationService;
    private final UserService userService;

    @Override
    public ResponseEntity<ApplicationRegisterResponse> postApplicationToken(UUID publicId) {
        log.info("ApplicationController: Starting registration for application, at: {}", DateTimeConverter.formatInstantNow());
        var registerApplication = applicationService.registerApplication(publicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerApplication);
    }

    @Override
    public ResponseEntity<ApplicationResponse> postCreateApplication(ApplicationCreateRequest applicationCreateRequest) {
        log.info("ApplicationController: Starting creation of application: {}, at: {}", applicationCreateRequest.getNameApplication(), DateTimeConverter.formatInstantNow());

        userService.validateAdminUser(extractJwtFromContext());

        validateEmailFormat(applicationCreateRequest.getApplicationEmail());

        var response = applicationService.createApplication(applicationCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
