package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.LoginApi;
import br.com.hahn.auth.application.service.AuthService;
import br.com.hahn.auth.domain.model.LogOfRequest;
import br.com.hahn.auth.domain.model.LoginRequest;
import br.com.hahn.auth.domain.model.LoginResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@Slf4j
@AllArgsConstructor
public class LoginController extends AbstractController implements LoginApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<LoginResponse> postLogin(LoginRequest loginRequest) {
        log.info("LoginController: Starting login for user {}, at {}", loginRequest.getEmail(), Instant.now());

        log.info("LoginController: Validate email format at: {}", Instant.now());
        validateEmailFormat(loginRequest.getEmail());

        log.info("LoginController: Validate password format at: {}", Instant.now());
        validatePasswordFormat(loginRequest.getPassword());
        
        var loginResponse = authService.userLogin(loginRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }

    @Override
    public ResponseEntity<Void> deleteLoggedUser(LogOfRequest logOfRequest) {
        log.info("LoginController: Starting logoff for user with email: {}, at {}", logOfRequest.getEmail(), Instant.now());
        authService.logOffUser(extractJwtFromContext());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
