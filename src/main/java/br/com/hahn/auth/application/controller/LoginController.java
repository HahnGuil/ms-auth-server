package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.LoginApi;
import br.com.hahn.auth.application.service.AuthService;
import br.com.hahn.auth.domain.model.LogOfRequest;
import br.com.hahn.auth.domain.model.LoginRequest;
import br.com.hahn.auth.domain.model.LoginResponse;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@AllArgsConstructor
public class LoginController extends AbstractController implements LoginApi {

    private final AuthService authService;

    /**
     * Handles the login request for a user.
     * <p>
     * This method processes the login request by:
     * - Logging the start of the login process.
     * - Validating the format of the provided email.
     * - Validating the format of the provided password.
     * - Delegating the login operation to the {@link AuthService}.
     * Upon successful login, it returns a {@link ResponseEntity} with the login response
     * and an HTTP status of 201 (Created).
     * </p>
     *
     * @author HahnGuil
     * @param loginRequest the login request containing the user's email and password
     * @return a {@link ResponseEntity} containing the login response and HTTP status
     */
    @Override
    public ResponseEntity<LoginResponse> postLogin(LoginRequest loginRequest) {
        log.info("LoginController: Starting login for user {}, at {}", loginRequest.getEmail(), DateTimeConverter.formatInstantNow());

        log.info("LoginController: Validate email format at: {}", DateTimeConverter.formatInstantNow());
        validateEmailFormat(loginRequest.getEmail());

        log.info("LoginController: Validate password format at: {}", DateTimeConverter.formatInstantNow());
        validatePasswordFormat(loginRequest.getPassword());
        
        var loginResponse = authService.userLogin(loginRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }

    /**
     * Handles the logoff request for a logged-in user.
     * <p>
     * This method processes the logoff request by:
     * - Logging the start of the logoff process with the user's email.
     * - Extracting the JWT from the current security context.
     * - Delegating the logoff operation to the {@link AuthService}.
     * Upon successful logoff, it returns a {@link ResponseEntity} with an HTTP status of 204 (No Content).
     * </p>
     *
     * @author HahnGuil
     * @param logOfRequest the logoff request containing the user's email
     * @return a {@link ResponseEntity} with an HTTP status of 204 (No Content)
     */
    @Override
    public ResponseEntity<Void> deleteLoggedUser(LogOfRequest logOfRequest) {
        log.info("LoginController: Starting logoff for user with email: {}, at {}", logOfRequest.getEmail(), DateTimeConverter.formatInstantNow());
        authService.logOffUser(extractJwtFromContext());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
