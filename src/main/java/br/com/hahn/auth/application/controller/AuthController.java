package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.application.dto.request.*;
import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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




    // TODO - APAGAR
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetePassword(@RequestBody PasswordOperationRequestDTO passwordOperationRequestDTO){
        log.info("AuthController: Resete password Request");
        String response = authService.resetPassword(passwordOperationRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }



}
