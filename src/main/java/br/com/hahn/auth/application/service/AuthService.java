package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.request.PasswordOperationRequestDTO;
import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.dto.response.ResetPasswordResponseDTO;
import br.com.hahn.auth.application.execption.*;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.model.LoginLog;
import br.com.hahn.auth.domain.model.LoginResponse;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.infrastructure.security.TokenService;
import br.com.hahn.auth.infrastructure.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService {

    private final Random random = new Random();

    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final UserService userService;
    private final ResetPasswordService resetPasswordService;
    private final LoginLogService loginLogService;

//    --------- Refatoração

    /**
     * Validates the user's credentials.
     * This method compares the provided login password with the user's stored password.
     * If the passwords do not match, an InvalidCredentialsException is thrown.
     *
     * @author HahnGuil
     * @param loginPassword the password provided during login
     * @param userPassword the user's stored password
     * @throws InvalidCredentialsException if the passwords do not match
     */
    public void validadeCredentials(String loginPassword, String userPassword) {
        if(!passwordEncoder.matches(loginPassword, userPassword)){
            throw new InvalidCredentialsException("Invalid email or password.");
        }
    }

    /**
     * Validates if the user is an OAuth user.
     * This method checks if the user's password is null or empty, indicating that the user
     * is attempting to log in using OAuth. If so, a DirectLoginNotAllowedException is thrown.
     *
     * @author HahnGuil
     * @param user the User object to be validated
     * @throws DirectLoginNotAllowedException if the user is an OAuth user attempting direct login
     */
    public void validatingYourUserIsOauth(User user){
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            log.error("AuthService: User: {} try login with OAuth, throw exception at {}", user.getUserId(), Instant.now());
            throw new DirectLoginNotAllowedException("OAuth users cannot log in directly.");
        }
    }

//    ---------------------------
// Código Antigo

    public LoginResponseDTO refreshAccessToken(String token) {
        log.info("AuthService: Validate Refresh Token and get email from user");
        var user = userService.findByEmail(tokenService.validateToken(token));

        log.info("AuthService: Check if token already use");
        this.checkTokenActive(tokenService.extracLoginLogId(token));

        loginLogService.deactivateActiveToken(user.getUserId(), TypeInvalidation.USER_REFRESH);

        log.info("AuthService: return renewed access token");
        var loginLog = loginLogService.saveLoginLog(user, ScopeToken.LOGIN_TOKEN, LocalDateTime.now());

        return new LoginResponseDTO(user.getFirstName(),
                user.getEmail(),
                tokenService.generateToken(user, loginLog),
                tokenService.generateRefreshToken(user, loginLog));
    }


    public void updatePassword(PasswordOperationRequestDTO request) {
        log.info("AuthService: Update password");
        User user = userService.findByEmail(request.email());

        validateOldPassword(request.oldPassword(), user.getPassword());

        userService.updatePassword(user.getEmail(), user.getUserId(), passwordEncoder.encode(request.newPassword()), LocalDateTime.now());
    }

    public String forgotPassword(PasswordOperationRequestDTO request) {
        log.info("AuthService: Forgot password");
        changeResetPassword(request.email());

        User user = userService.findByEmail(request.email());
        String recoverCode = generateRecoverCode();

        resetPasswordService.createResetPassword(user, encodePassword(recoverCode));
        sendEmail(user.getEmail(), buildResetEmailBody(user.getUsername(), recoverCode));
        log.info("AuthService: send reset code to email");

        return "A validation code has been sent to the email address you provided. Please also check your spam folder. Expect an email from noreply@toxicbet.com.br.";
    }

    public ResetPasswordResponseDTO validateRecoverCode(PasswordOperationRequestDTO requestRecoverCode) {
        log.info("AuthService: validate recorver token");
        ResetPassword resetPassword = resetPasswordService.findByEmail(requestRecoverCode.email());
        resetPasswordService.validateTokenExpiration(requestRecoverCode.recoverCode());
        validateRecoverCodeValues(resetPassword, requestRecoverCode);
        return new ResetPasswordResponseDTO(tokenService.generateRecorverToken(resetPassword));
    }

    // TODO - APAGAR
    public String resetPassword(PasswordOperationRequestDTO passwordOperationRequestDTO) {
        log.info("AuthService: Reset password");
        ResetPassword resetPassword = resetPasswordService.findByEmail(passwordOperationRequestDTO.email());
        User user = userService.findByEmail(resetPassword.getUserEmail());
        userService.updatePassword(user.getEmail(), user.getUserId(), passwordOperationRequestDTO.newPassword(), LocalDateTime.now());
        return "Password reset successfully";
    }

    // TODO - REFATORAR
    private void validateOldPassword(String oldPassword, String currentPassword) {
        log.info("AuthService: validate old password");
        if (validadeCredentials(oldPassword, currentPassword)) {
            log.error("AuthService: Invalid credentials, throw exception");
            throw new InvalidCredentialsException("Invalid credencials");
        }
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private void validateRecoverCodeValues(ResetPassword resetPassword, PasswordOperationRequestDTO requestRecoverCode){
        log.info("AuthService: Validate values of recover code");
        if(!passwordEncoder.matches(requestRecoverCode.recoverCode(), resetPassword.getRecoverCode())){
            log.error("AuthService: recover code not match, throw exception");
            throw new InvalidRecorveCodeExcpetion("The validation code provided is not valid.");
        }

        if(resetPassword.getExpirationDate().isBefore(LocalDateTime.now())){
            log.error("AuthService: Recover code has expired, throw exception");
            throw new InvalidRecorveCodeExcpetion("Recovery code has expired.");
        }
    }

    private String generateRecoverCode() {
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private String buildResetEmailBody(String username, String recoverCode) {
        return String.format("<p>Hello %s,</p><p>Your password reset code is: <strong>%s</strong></p><p>This code will expire in 30 minutes.</p>", username, recoverCode);
    }



    private void sendEmail(String email, String htmlBody) {
        log.info("AuthService: Send email to recorver code");
        try {
            emailService.sendEmail(email, "Password Reset Request", htmlBody).block();
        } catch (Exception _) {
            log.error("AuthService: Failed to send email");
            throw new InvalidOperationExecption("Failed to send email. Please try again later.");
        }
    }



    private void changeResetPassword(String email){
        if(resetPasswordService.existsByUserEmail(email)){
            resetPasswordService.deleteResetExistingPassword(email);
        }
    }

    private void checkTokenActive (UUID loginLogId){
        if(!loginLogService.isTokenValid(loginLogId)){
            throw new InvalidCredentialsException("Token expired, need to log in again");
        }
    }
}