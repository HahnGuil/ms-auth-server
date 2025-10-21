package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.request.LoginRequestDTO;
import br.com.hahn.auth.application.dto.request.PasswordOperationRequestDTO;
import br.com.hahn.auth.application.dto.request.UserRequestDTO;
import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.dto.response.ResetPasswordResponseDTO;
import br.com.hahn.auth.application.dto.response.UserResponseDTO;
import br.com.hahn.auth.application.execption.*;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.model.LoginLog;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.infrastructure.security.TokenService;
import br.com.hahn.auth.infrastructure.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

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


    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        log.info("AuthService: Create user");
        User user = userService.convertToEntity(userRequestDTO,encodePassword(userRequestDTO.password()));

        if(userService.existsByEmail(user.getEmail())){
            throw new UserAlreadyExistException("Email already registered. Please log in or recover your password.");
        }

        userService.saveUser(user);

        return new UserResponseDTO(user.getUsername(), user.getEmail(), tokenService.generateToken(user, loginLogService.saveLoginLog(user, ScopeToken.REGISTER_TOKEN, LocalDateTime.now())));
    }

    public LoginResponseDTO userLogin(LoginRequestDTO bodyRequest) {
        log.info("AuthService: Login user");
        User user = userService.findByEmail(bodyRequest.email());

        if(Boolean.TRUE.equals(user.getBlockUser())){
            log.info("AuthService: User blocl, throw exception");
            throw new UserBlockException("This user has been blocked. Use the password reset link.");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            log.info("AuthService: User try login with OAuth, throw exception");
            throw new InvalidCredentialsException("Direct login is not allowed for users created via OAuth.");
        }

        if (validadeCredentials(bodyRequest.password(), user.getPassword())) {
            log.info("AuthService: invalid credencials, throw exception");
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        LoginLog loginLog = loginLogService.saveLoginLog(user, ScopeToken.LOGIN_TOKEN, LocalDateTime.now());

        log.info("AuthService: Login success");
        return new LoginResponseDTO(user.getEmail(),
                tokenService.generateToken(user, loginLog),
                tokenService.generateRefreshToken(user, loginLog));
    }

    public LoginResponseDTO refreshAccessToken(String token) {
        log.info("AuthService: Validate Refresh Token and get email from user");
        var user = userService.findByEmail(tokenService.validateToken(token));

        log.info("AuthService: Check if token already use");
        this.checkTokenActive(tokenService.extracLoginLogId(token));

        loginLogService.deactivateActiveToken(user.getUserId(), TypeInvalidation.USER_REFRESH);

        log.info("AuthService: return renewed access token");
        var loginLog = loginLogService.saveLoginLog(user, ScopeToken.LOGIN_TOKEN, LocalDateTime.now());

        return new LoginResponseDTO(user.getEmail(),
                tokenService.generateToken(user, loginLog),
                tokenService.generateRefreshToken(user, loginLog));
    }


    public LoginResponseDTO processOAuth2User(OAuth2User oAuth2User) {
        log.info("AuthService: Process OAuth User");
        String email = oAuth2User.getAttribute("email");
        User user;
        LoginLog loginLog;

        if(userService.existsByEmail(email)){
            log.info("AuthService: User exist, login with OAuth");
            user = userService.findByIdWithApplications(email);
            loginLog = loginLogService.saveLoginLog(user, ScopeToken.LOGIN_TOKEN, LocalDateTime.now());
        } else {
            log.info("AuthService: User not exist, create user of OAuth");
            user = createNewUserFromOAuth(oAuth2User);
            loginLog = loginLogService.saveLoginLog(user, ScopeToken.REGISTER_TOKEN, LocalDateTime.now());
        }

        return new LoginResponseDTO(user.getEmail(),
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

        return "Password reset code sent to your email.";
    }

    public ResetPasswordResponseDTO validateRecoverCode(PasswordOperationRequestDTO requestRecoverCode) {
        log.info("AuthService: validate recorver token");
        ResetPassword resetPassword = resetPasswordService.findByEmail(requestRecoverCode.email());
        resetPasswordService.validateTokenExpiration(requestRecoverCode.recoverCode());
        validateRecoverCodeValues(resetPassword, requestRecoverCode);
        return new ResetPasswordResponseDTO(tokenService.generateRecorverToken(resetPassword));
    }

    public String resetPassword(PasswordOperationRequestDTO passwordOperationRequestDTO) {
        log.info("AuthService: Reset password");
        ResetPassword resetPassword = resetPasswordService.findByEmail(passwordOperationRequestDTO.email());
        User user = userService.findByEmail(resetPassword.getUserEmail());
        userService.updatePassword(user.getEmail(), user.getUserId(), passwordOperationRequestDTO.newPassword(), LocalDateTime.now());
        return "Password reset successfully";
    }

    public User createNewUserFromOAuth(OAuth2User oAuth2User) {
        UserRequestDTO userRequestDTO = userService.convertOAuthUserToRequestDTO(oAuth2User);
        User newUser = userService.convertToEntity(userRequestDTO, encodePassword(""));
        userService.saveUser(newUser);
        return newUser;
    }

    private void validateOldPassword(String oldPassword, String currentPassword) {
        log.info("AuthService: validate old password");
        if (validadeCredentials(oldPassword, currentPassword)) {
            log.info("AuthService: Invalid credentials, throw exception");
            throw new InvalidCredentialsException("Invalid credencials");
        }
    }

    private void validateRecoverCodeValues(ResetPassword resetPassword, PasswordOperationRequestDTO requestRecoverCode){
        log.info("AuthService: Validate values of recover code");
        if(!passwordEncoder.matches(requestRecoverCode.recoverCode(), resetPassword.getRecoverCode())){
            log.info("AuthService: recover code not match, throw exception");
            throw new InvalidRecorveCodeExcpetion("Invalid recovery code.");
        }

        if(resetPassword.getExpirationDate().isBefore(LocalDateTime.now())){
            log.info("AuthService: Recover code has expired, throw exception");
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

    private boolean validadeCredentials(String loginPassword, String userPassword) {
        return !passwordEncoder.matches(loginPassword, userPassword);
    }

    private void sendEmail(String email, String htmlBody) {
        log.info("AuthService: Send email to recorver code");
        try {
            emailService.sendEmail(email, "Password Reset Request", htmlBody).block();
        } catch (Exception _) {
            throw new InvalidOperationExecption("Failed to send email. Please try again later.");
        }
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
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