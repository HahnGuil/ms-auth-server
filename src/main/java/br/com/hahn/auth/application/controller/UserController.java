package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.UsersApi;
import br.com.hahn.auth.application.service.UserService;
import br.com.hahn.auth.domain.model.UserRequest;
import br.com.hahn.auth.domain.model.UserResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/register")
@Slf4j
@AllArgsConstructor
public class UserController implements UsersApi {

    private final UserService userService;


    @Override
    public ResponseEntity<UserResponse> postRegisterUser(UserRequest userRequest) {
        log.info("UserController: Starting user registration fot user {}, at {}", userRequest.getEmail(), Instant.now());
        var userResponse = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}
