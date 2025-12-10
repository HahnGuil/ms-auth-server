package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.PasswordApi;
import br.com.hahn.auth.application.service.PasswordService;
import br.com.hahn.auth.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PasswordController extends AbstractController implements PasswordApi {


    private final PasswordService passwordService;

    @Override
    public ResponseEntity<Void> patchChangePassword(ChangePasswordRequest changePasswordRequest) {
        log.info("PasswordController: Starting changePassword for user with email: {}, at: {}", changePasswordRequest.getEmail(), Instant.now());
        passwordService.changePassword(changePasswordRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<SuccessResponse> postPasswordResetRequest(PasswordResetRequest passwordResetRequest) {
        log.info("PasswordController: Starting generate validate code flow for user email: {}, at: {}", passwordResetRequest.getEmail(), Instant.now());
        var response = passwordService.requestValidateCode(passwordResetRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    public ResponseEntity<ValidateCodeResponse> postRequestValidateCode(ValidateCodeRequest validateCodeRequest) {
        log.info("PasswordController: Starting generate validate for recovery code to user: {}, at: {}", validateCodeRequest.getEmail(), Instant.now());
        var response = passwordService.validateResetCode(validateCodeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<Void> patchResetPassword(NewPasswordRequest newPasswordRequest) {
        passwordService.resetUserPassword(extractJwtFromContext(), newPasswordRequest);
        log.info("PasswordController: Stating reset password at: {}", Instant.now());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
