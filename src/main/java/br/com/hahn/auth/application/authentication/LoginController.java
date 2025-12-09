package br.com.hahn.auth.application.authentication;

import br.com.hahn.auth.LoginApi;
import br.com.hahn.auth.application.service.LoginService;
import br.com.hahn.auth.domain.model.LogOfRequest;
import br.com.hahn.auth.domain.model.LoginRequest;
import br.com.hahn.auth.domain.model.LoginResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/login")
@Slf4j
@AllArgsConstructor
public class LoginController implements LoginApi {

    private final LoginService loginService;

    @Override
    public ResponseEntity<LoginResponse> postLogin(LoginRequest loginRequest) {
        log.info("LoginController: Starting login for user {}, at {}", loginRequest.getEmail(), Instant.now());
        var loginResponse = loginService.userLogin(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @Override
    public ResponseEntity<Void> deleteLoggedUser(LogOfRequest logOfRequest) {
        log.info("LoginController: Starting logoff for user with email: {}, at {}", logOfRequest.getEmail(), Instant.now());
        loginService.logOffUser((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
