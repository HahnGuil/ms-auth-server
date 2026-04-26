package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.ApplicationApi;
import br.com.hahn.auth.application.service.ApplicationService;
import br.com.hahn.auth.domain.model.ApplicationRegisterResponse;
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
public class ApplicationController implements ApplicationApi {

    private final ApplicationService applicationService;

    @Override
    public ResponseEntity<ApplicationRegisterResponse> postApplicationToken(UUID publicId) {
        log.info("ApplicationController: Starting registration for application, at: {}", DateTimeConverter.formatInstantNow());
        var registerApplication = applicationService.registerApplication(publicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerApplication);
    }
}