package br.com.hahn.auth.application.authentication;

import br.com.hahn.auth.application.dto.request.LoginRequestDTO;
import br.com.hahn.auth.application.dto.request.UserRequestDTO;
import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.dto.response.UserResponseDTO;
import br.com.hahn.auth.application.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserRequestDTO body) {

        userService.existsByEmail(body.email());

        UserResponseDTO userResponseDTO = userService.createUser(body);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User successfully registered", "user", userResponseDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO body) {
        LoginResponseDTO loginResponseDTO = userService.userlogin(body);
        return ResponseEntity.ok(loginResponseDTO);
    }
}
