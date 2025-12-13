package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.UsersApi;
import br.com.hahn.auth.application.service.UserService;
import br.com.hahn.auth.domain.model.UserRequest;
import br.com.hahn.auth.domain.model.UserResponse;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@AllArgsConstructor
public class UserController extends AbstractController implements UsersApi {

    private final UserService userService;

    /**
     * Handles the registration of a new user.
     * <p>
     * This method processes the user registration request by:
     * - Logging the start of the registration process with the user's email.
     * - Validating the format of the provided email.
     * - Validating the format of the provided password.
     * - Delegating the user creation and login operation to the {@link UserService}.
     * Upon successful registration, it returns a {@link ResponseEntity} containing a {@link UserResponse}
     * and an HTTP status of 201 (Created).
     * </p>
     *
     * @author HahnGuil
     * @param userRequest the request containing the user's email and password
     * @return a {@link ResponseEntity} containing a {@link UserResponse} and HTTP status of 201 (Created)
     */
    @Override
    public ResponseEntity<UserResponse> postRegisterUser(UserRequest userRequest) {
        log.info("UserController: Starting user registration fot user {}, at {}", userRequest.getEmail(), DateTimeConverter.formatInstantNow());

        log.info("UserController: Validate email format at: {}", DateTimeConverter.formatInstantNow());
        validateEmailFormat(userRequest.getEmail());

        log.info("UserController: Validate password format at: {}", DateTimeConverter.formatInstantNow());
        validatePasswordFormat(userRequest.getPassword());

        log.info("Calling UserService to create and log in the user at: {}", DateTimeConverter.formatInstantNow());
        var userResponse = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}