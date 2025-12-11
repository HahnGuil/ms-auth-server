package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.UsersApi;
import br.com.hahn.auth.application.service.UserService;
import br.com.hahn.auth.domain.model.UserRequest;
import br.com.hahn.auth.domain.model.UserResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@Slf4j
@AllArgsConstructor
public class UserController extends AbstractController implements UsersApi {

    private final UserService userService;


    @Override
    public ResponseEntity<UserResponse> postRegisterUser(UserRequest userRequest) {
        log.info("UserController: Starting user registration fot user {}, at {}", userRequest.getEmail(), Instant.now());

        log.info("UserController: Validate email format at: {}", Instant.now());
        validateEmailFormat(userRequest.getEmail());

        log.info("UserController: Validate password format at: {}", Instant.now());
        validatePasswordFormat(userRequest.getPassword());

        log.info("Calling UserService to create and log in the user at: {}", Instant.now());
        var userResponse = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}
