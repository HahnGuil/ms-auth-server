package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.PasswordApi;
import br.com.hahn.auth.application.service.PasswordService;
import br.com.hahn.auth.domain.model.*;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PasswordController extends AbstractController implements PasswordApi {


    private final PasswordService passwordService;

    /**
     * Handles the password change request for a user.
     * <p>
     * This method processes the password change request by:
     * - Logging the start of the process with the user's email.
     * - Validating the format of the provided email.
     * - Validating the format of the new password.
     * - Validating the format of the old password.
     * - Validating if the token is valid for the user.
     * - Delegating the password change operation to the {@link PasswordService}.
     * Upon successful completion, it returns a {@link ResponseEntity} with an HTTP status of 204 (No Content).
     * </p>
     *
     * @author HahnGuil
     * @param changePasswordRequest the request containing the user's email, old password, and new password
     * @return a {@link ResponseEntity} with an HTTP status of 204 (No Content)
     */
    @Override
    public ResponseEntity<Void> patchChangePassword(ChangePasswordRequest changePasswordRequest) {
        log.info("PasswordController: Starting changePassword for user with email: {}, at: {}", changePasswordRequest.getEmail(), DateTimeConverter.formatInstantNow());

        log.info("PasswordController: Validate email format at: {}", DateTimeConverter.formatInstantNow());
        validateEmailFormat(changePasswordRequest.getEmail());

        log.info("PasswordController: Validate new password format at: {}", DateTimeConverter.formatInstantNow());
        validatePasswordFormat(changePasswordRequest.getNewPassword());

        log.info("PasswordController: Validate old password format at: {}", DateTimeConverter.formatInstantNow());
        validatePasswordFormat(changePasswordRequest.getOldPassword());

        log.info("PasswordController: validate if the token is valid for user: {}, at: {}", changePasswordRequest.getEmail(), DateTimeConverter.formatInstantNow());
        passwordService.validateTokenForChangePassword(extractJwtFromContext());

        passwordService.changePassword(changePasswordRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Handles the password reset request for a user.
     * <p>
     * This method processes the password reset request by:
     * - Logging the start of the process with the user's email.
     * - Validating the format of the provided email.
     * - Delegating the request to generate a validation code to the {@link PasswordService}.
     * Upon successful completion, it returns a {@link ResponseEntity} containing a {@link SuccessResponse}
     * and an HTTP status of 200 (OK).
     * </p>
     *
     * @author HahnGuil
     * @param passwordResetRequest the request containing the user's email for password reset
     * @return a {@link ResponseEntity} containing a {@link SuccessResponse} and HTTP status of 200 (OK)
     */
    @Override
    public ResponseEntity<SuccessResponse> postPasswordResetRequest(PasswordResetRequest passwordResetRequest) {
        log.info("PasswordController: Starting generate validate code flow for user email: {}, at: {}", passwordResetRequest.getEmail(), DateTimeConverter.formatInstantNow());

        log.info("PasswordController: Validate format email of reset request at: {}", DateTimeConverter.formatInstantNow());
        validateEmailFormat(passwordResetRequest.getEmail());

        log.info("PasswordController: Validate cod send to user email: {}, at: {}", passwordResetRequest.getEmail(), DateTimeConverter.formatInstantNow());
        var response = passwordService.requestValidateCode(passwordResetRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Handles the validation of a recovery code for a user.
     * <p>
     * This method processes the request to validate a recovery code by:
     * - Logging the start of the validation process with the user's email.
     * - Delegating the validation of the recovery code to the {@link PasswordService}.
     * Upon successful validation, it returns a {@link ResponseEntity} containing a {@link ValidateCodeResponse}
     * and an HTTP status of 201 (Created).
     * </p>
     *
     * @author HahnGuil
     * @param validateCodeRequest the request containing the user's email and recovery code to be validated
     * @return a {@link ResponseEntity} containing a {@link ValidateCodeResponse} and HTTP status of 201 (Created)
     */
    @Override
    public ResponseEntity<ValidateCodeResponse> postRequestValidateCode(ValidateCodeRequest validateCodeRequest) {
        log.info("PasswordController: Starting generate validate for recovery code to user: {}, at: {}", validateCodeRequest.getEmail(), DateTimeConverter.formatInstantNow());

        var response = passwordService.validateResetCode(validateCodeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Handles the password reset process for a user.
     * <p>
     * This method processes the password reset request by:
     * - Logging the start of the process.
     * - Validating the format of the new password.
     * - Delegating the password reset operation to the {@link PasswordService}.
     * Upon successful completion, it returns a {@link ResponseEntity} with an HTTP status of 204 (No Content).
     * </p>
     *
     * @author HahnGuil
     * @param newPasswordRequest the request containing the new password for the user
     * @return a {@link ResponseEntity} with an HTTP status of 204 (No Content)
     */
    @Override
    public ResponseEntity<Void> patchResetPassword(NewPasswordRequest newPasswordRequest) {
        log.info("PasswordController: password format at: {}", DateTimeConverter.formatInstantNow());
        validatePasswordFormat(newPasswordRequest.getNewPassword());

        log.info("PasswordController: Stating reset password at: {}", DateTimeConverter.formatInstantNow());
        passwordService.resetUserPassword(extractJwtFromContext(), newPasswordRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}