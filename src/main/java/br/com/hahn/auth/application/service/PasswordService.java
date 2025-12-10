package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.*;
import br.com.hahn.auth.domain.enums.*;
import br.com.hahn.auth.domain.model.*;
import br.com.hahn.auth.domain.respository.ResetPasswordRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import br.com.hahn.auth.infrastructure.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class PasswordService {

    private final Random random = new Random();

    private final UserService userService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final ResetPasswordRepository resetPasswordRepository;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final TokenLogService tokenLogService;


    public void changePassword(ChangePasswordRequest changePasswordRequest){
        log.info("PasswordService: Starting change password for user with email: {} at: {}", changePasswordRequest.getEmail(), Instant.now());

        log.info("PasswordService: Call User service to find the user by email: {} at: {}", changePasswordRequest.getEmail(), Instant.now());
        var user = userService.findByEmail(changePasswordRequest.getEmail());

        log.info("PasswordService: Check if the user: {} is OAuth at: {}", user.getUserId(), Instant.now());
        checkIfItIsAuthUser(user);

        log.info("PasswordService: Call AuthService, to validate the oldPassword for user: {}, at: {}", user.getUserId(), Instant.now());
        authService.validateOldPassword(user, changePasswordRequest.getOldPassword());

        log.info("PasswordService: Call UserService to update user password to user: {} at: {}", user.getUserId(), Instant.now());
        userService.updatePassword(user.getEmail(), user.getUserId(), passwordEncoder.encode(changePasswordRequest.getNewPassword()), LocalDateTime.now());

    }

    public SuccessResponse requestValidateCode(PasswordResetRequest passwordResetRequest){
        log.info("PasswordService: Starting requesting for a validate code for email: {}, at: {}", passwordResetRequest.getEmail(), Instant.now());

        log.info("PasswordService: Find user by email: {}, at: {}", passwordResetRequest.getEmail(), Instant.now());
        var user = userService.findByEmail(passwordResetRequest.getEmail());

        log.info("PasswordService: Verify if already exists one change reset password for this email: {}, at: {}", passwordResetRequest.getEmail(), Instant.now());
        findAnDeleteResetPassword(passwordResetRequest);
        
        log.info("PasswordService: Creating recover code for user: {}, at: {}", user.getUserId(), Instant.now());
        var recoverCode = generateRecoverCode();
        
        log.info("PasswordService: Creating reset password for the user: {}, at: {}", user.getUserId(), Instant.now());
        createResetPassword(user, passwordEncoder.encode(recoverCode));
        
        log.info("PasswordService: Send validation code to user email: {}, at: {}", user.getEmail(), Instant.now());
        sendEmail(user.getEmail(), buildResetEmailBody(user.getFirstName() + " " +  user.getLastName(), recoverCode));

        return generateResponse();
    }

    public ValidateCodeResponse validateResetCode(ValidateCodeRequest validateCodeRequest){
        log.info("PasswordService: Starting validating recover code for user with email: {}, at: {}", validateCodeRequest.getEmail(), Instant.now());

        log.info("PasswordService: Find recover code for user email: {}, at: {}", validateCodeRequest.getEmail(), Instant.now());
        var resetPassword = findResetPasswordByEmail(validateCodeRequest.getEmail());

        log.info("PasswordService: Validate if the recovery code of user email: {}, is not expired, at: {}", validateCodeRequest.getEmail(), Instant.now());
        validateRecoverCodeValues(resetPassword, validateCodeRequest);

        log.info("PasswordService: Create TokenLog for request of user: {}, at: {}", validateCodeRequest.getEmail(), Instant.now());
        var tokenLog = tokenLogService.saveTokenLog(userService.findByEmail(resetPassword.getUserEmail()), ScopeToken.RECOVER_CODE, LocalDateTime.now());

        log.info("PasswordService: Generate Recover token for user: {}, at: {}", resetPassword.getUserEmail(), Instant.now());
        return generateRecoveryToken(tokenService.generateRecorverToken(resetPassword, tokenLog));

    }

    public void resetUserPassword(Jwt jwt, NewPasswordRequest newPasswordRequest){
        log.info("PasswordService: Starting reset password for user with email: {}, at: {}", jwt.getSubject(), Instant.now());
        validateScopeFromRecoveryToken(jwt.getClaim("scope"));

        log.info("PasswordService: Extract user id and email from jwt token at: {}", Instant.now());
        var userId = UUID.fromString(jwt.getClaim("user_id"));
        var userEmail = jwt.getSubject();

        log.info("PasswordService: User id: {}, and user email: {}, were successfully extracted at: {}", userId, userEmail, Instant.now());

        log.info("PasswordService: Call UserService for reset password from User: {} at: {}", userId, Instant.now());
        userService.updatePassword(userEmail, userId, passwordEncoder.encode(newPasswordRequest.getNewPassword()), LocalDateTime.now());

    }

    private void validateScopeFromRecoveryToken(String scopeToken){
        if(!ScopeToken.RECOVER_CODE.getValue().equals(scopeToken)){
            log.info("PasswordService: Scope Token is not valid for reset password. Throw InvalidRecoverTokenException at: {}", Instant.now());
            throw new InvalidRecoverTokenException(ErrorsResponses.INVALID_RECOVERY_CODE.getMessage());
        }
    }

    // TODO -- VERIFICAR ROTINA DE APAGAR REQUEST DE RESET DE SENHA
    public int deleteByExpirationDateBefore(LocalDateTime dataTime){
        log.info("ResetPasswordService: Delete reset password by expiration date");
        return resetPasswordRepository.deleteByExpirationDateBefore(dataTime);
    }

    private ValidateCodeResponse generateRecoveryToken(String recoveryToken){
        ValidateCodeResponse validateCodeResponse = new ValidateCodeResponse();
        validateCodeResponse.setRecoverToken(recoveryToken);
        return validateCodeResponse;
    }

    private void validateRecoverCodeValues(ResetPassword resetPassword, ValidateCodeRequest validateCodeRequest){
        log.info("PasswordService: Validate if the recover code of request of the user: {}, matches, at: {}", validateCodeRequest.getEmail(), Instant.now());
        if(!passwordEncoder.matches(validateCodeRequest.getRecoveryCode() , resetPassword.getRecoverCode())){
            log.error("PasswordService: Recovery code of user: {}, not Match. Throw InvalidRecoverCodeException at: {}", validateCodeRequest.getEmail(), Instant.now());
            throw new InvalidRecoverCodeException(ErrorsResponses.INVALID_RECOVERY_CODE.getMessage());
        }
        validateRecoverCodeExpirationTime(resetPassword);
    }

    private void validateRecoverCodeExpirationTime(ResetPassword resetPassword){
        log.info("PasswordService: Validate recover code expiration time, for user email: {}, at: {}", resetPassword.getUserEmail() , Instant.now());
        if(resetPassword.getExpirationDate().isBefore(LocalDateTime.now())){
            log.error("PasswordService: Recovery Code of user {}, is expired. Throw InvalidRecoverCodeException at: {}", resetPassword.getUserEmail(), Instant.now());
            throw new InvalidRecoverCodeException(ErrorsResponses.EXPIRED_RECOVERY_CODE.getMessage());
        }

    }

    private void createResetPassword(User user, String recoverCode){
        log.info("ResetPasswordService: Create reset password for user: {}, at: {}", user.getUserId(), Instant.now());
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setRecoverCode(recoverCode);
        resetPassword.setUserEmail(user.getEmail());
        resetPassword.setExpirationDate(LocalDateTime.now().plusMinutes(30));
        resetPasswordRepository.save(resetPassword);
    }

    private void checkIfItIsAuthUser(User user){
        if (user.getTypeUser().equals(TypeUser.OAUTH_USER)){
            log.error("PasswordService: User: {}, is OUATH_USER: {}. Throw the UserCanNotChangePasswordException at: {}", user.getUserId(), user.getTypeUser(), Instant.now());
            throw new UserCanNotChangePasswordException(ErrorsResponses.CHANGE_PASSWORD_NOT_ALLOWED_FOR_OAUTH_USER.getMessage());
        }
    }

    private void findAnDeleteResetPassword(PasswordResetRequest passwordResetRequest){
        if(resetPasswordRepository.existsByUserEmail(passwordResetRequest.getEmail())){
            var resetPassword = findResetPasswordByEmail(passwordResetRequest.getEmail());
            resetPasswordRepository.deleteById(resetPassword.getId());
        }
    }

    private String generateRecoverCode() {
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private SuccessResponse generateResponse(){
        log.info("PasswordService: Build success response at: {}", Instant.now());
        SuccessResponse response = new SuccessResponse();
        response.setMessage(SuccessResponses.SEND_RECOVERY_CODE_TO_EMAIL.getMessage());
        return response;
    }

    private void sendEmail(String email, String htmlBody) {
        log.info("PasswordService: Send email with validate code for user email: {}, at: {}", email, Instant.now());
        try {
            emailService.sendEmail(email, "Password Reset Request", htmlBody).block();
        } catch (Exception _) {
            log.error("PasswordService: Fail to send email to user email: {}. Throw InvalidOperationException at: {}", email, Instant.now());
            log.error("AuthService: Failed to send email");
            throw new InvalidOperationExecption(ErrorsResponses.FAIL_TO_SEND_EMAIL.getMessage());
        }
    }

    private ResetPassword findResetPasswordByEmail(String email){
        log.info("PasswordService: Find resetPassword for user email: {}, at: {}", email, Instant.now());
        return resetPasswordRepository.findByUserEmail(email).orElseThrow(() -> {
            log.error("PasswordService: Not found validation code to reset password for the user with email: {}. Throw NotFoundResetPasswordRequestForUser at: {}", email, Instant.now());
            return new NotFoundResetPasswordRequestForUser("There is no registered user for this email address. Check email and password or register a new user.");
        });
    }

    private String buildResetEmailBody(String username, String recoverCode) {
        log.error("PasswordService: Build email body to user name: {}, at: {}", username, Instant.now());
        return String.format("<p>Hello %s,</p><p>Your password reset code is: <strong>%s</strong></p><p>This code will expire in 30 minutes.</p>", username, recoverCode);
    }
}