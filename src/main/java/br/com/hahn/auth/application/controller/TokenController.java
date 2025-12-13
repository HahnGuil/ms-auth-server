package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.TokenApi;
import br.com.hahn.auth.application.service.AuthService;
import br.com.hahn.auth.domain.model.LoginResponse;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TokenController extends AbstractController implements TokenApi {

    private final AuthService authService;

    /**
     * Handles the refresh token process for a user.
     * <p>
     * This method processes the refresh token request by:
     * - Logging the start of the refresh token process.
     * - Delegating the generation of a new token to the {@link AuthService}.
     * Upon successful completion, it returns a {@link ResponseEntity} containing a {@link LoginResponse}
     * and an HTTP status of 201 (Created).
     * </p>
     *
     * @author HahnGuil
     * @return a {@link ResponseEntity} containing a {@link LoginResponse} and HTTP status of 201 (Created)
     */
    @Override
    public ResponseEntity<LoginResponse> postRefreshToken() {
        log.info("AuthController: Starting refresh token process at: {}", DateTimeConverter.formatInstantNow());
        var response = authService.generateNewTokenForUser(extractJwtFromContext());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

