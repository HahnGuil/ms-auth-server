package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.TokenApi;
import br.com.hahn.auth.application.service.AuthService;
import br.com.hahn.auth.domain.model.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TokenController extends AbstractController implements TokenApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<LoginResponse> postRefreshToken() {
        log.info("AuthController: Starting refresh token process at: {}", Instant.now());
        var response = authService.generateNewTokenForUser(extractJwtFromContext());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}
