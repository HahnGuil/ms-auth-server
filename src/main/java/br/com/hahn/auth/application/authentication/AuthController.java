package br.com.hahn.auth.application.authentication;

import br.com.hahn.auth.application.dto.request.*;
import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.dto.response.ResetPasswordResponseDTO;
import br.com.hahn.auth.application.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    // TOKEN CONTROLLER
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestHeader("Authorization") String authorizationHeader) {
        log.info("AuthController: Refresh Token Request");
        String token = authorizationHeader.replace("Bearer ", "");
        LoginResponseDTO renewedToken = authService.refreshAccessToken(token);
        return ResponseEntity.status(HttpStatus.OK).body(renewedToken);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody PasswordOperationRequestDTO request) {
        log.info("AuthController: Chance passwor request");
        authService.updatePassword(request);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Password successfully changed"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody PasswordOperationRequestDTO request) {
        log.info("AutoController: Forgot password request");
        String response = authService.forgotPassword(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/validate-recoverToken")
    public ResponseEntity<ResetPasswordResponseDTO> validateToken(@RequestBody PasswordOperationRequestDTO passwordOperationRequestDTO){
        log.info("AuthController: Validate token request");
        ResetPasswordResponseDTO resetPasswordResponseDTO = authService.validateRecoverCode(passwordOperationRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(resetPasswordResponseDTO);
    }

    // TODO - APAGAR
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetePassword(@RequestBody PasswordOperationRequestDTO passwordOperationRequestDTO){
        log.info("AuthController: Resete password Request");
        String response = authService.resetPassword(passwordOperationRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }



}
