package br.com.hahn.auth.application.authentication;

import br.com.hahn.auth.application.dto.request.*;
import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.dto.response.ResetPasswordResponseDTO;
import br.com.hahn.auth.application.dto.response.UserResponseDTO;
import br.com.hahn.auth.application.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserRequestDTO body) {
        logger.info("AuthController: Register user");
        authService.existsUserByEmail(body.email());
        UserResponseDTO userResponseDTO = authService.createUser(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User successfully registered", "user", userResponseDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO body) {
        logger.info("AuthController: Login user");
        LoginResponseDTO loginResponseDTO = authService.userLogin(body);
        return ResponseEntity.status(HttpStatus.OK).body(loginResponseDTO);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@RequestBody String refreshToken) {
        logger.info("AuthController: Refresh Token Request");
        String newAccessToken = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(newAccessToken);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody PasswordOperationRequestDTO request) {
        logger.info("AuthController: Chance passwor request");
        authService.updatePassword(request);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Password successfully changed"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody PasswordOperationRequestDTO request) {
        logger.info("AutoController: Forgot password request");
        String response = authService.forgotPassword(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/validate-recoverToken")
    public ResponseEntity<ResetPasswordResponseDTO> validateToken(@RequestBody PasswordOperationRequestDTO passwordOperationRequestDTO){
        logger.info("AuthController: Validate token request");
        ResetPasswordResponseDTO resetPasswordResponseDTO = authService.validateRecoverCode(passwordOperationRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(resetPasswordResponseDTO);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetePassword(@RequestBody PasswordOperationRequestDTO passwordOperationRequestDTO){
        logger.info("AuthController: Resete password Request");
        String response = authService.resetPassword(passwordOperationRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }



}
