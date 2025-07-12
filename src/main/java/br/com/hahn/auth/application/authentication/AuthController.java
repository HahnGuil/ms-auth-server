package br.com.hahn.auth.application.authentication;

import br.com.hahn.auth.application.dto.request.ChangePasswordRequestDTO;
import br.com.hahn.auth.application.dto.request.LoginRequestDTO;
import br.com.hahn.auth.application.dto.request.ResetPasswordRequestDTO;
import br.com.hahn.auth.application.dto.request.UserRequestDTO;
import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.dto.response.UserResponseDTO;
import br.com.hahn.auth.application.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(loginResponseDTO);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@RequestBody String refreshToken) {
        String newAccessToken = userService.refreshAccessToken(refreshToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(newAccessToken);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody ChangePasswordRequestDTO changePasswordRequestDTO){
        userService.updatePassword(changePasswordRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Password successfully changed"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> resetPassord(@RequestBody String email) {
        String response = userService.requestResetPassword(email);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
