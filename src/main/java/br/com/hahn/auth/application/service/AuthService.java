package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.request.*;
import br.com.hahn.auth.application.dto.response.LoginLogResponseDTO;
import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.dto.response.ResetPasswordResponseDTO;
import br.com.hahn.auth.application.dto.response.UserResponseDTO;
import br.com.hahn.auth.application.execption.*;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.infrastructure.security.TokenService;
import br.com.hahn.auth.infrastructure.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final Random random = new Random();

    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final UserService userService;
    private final ResetPasswordService resetPasswordService;
    private final LoginLogService loginLogService;


    public AuthService(PasswordEncoder passwordEncoder, TokenService tokenService, EmailService emailService, UserService userService, ResetPasswordService resetPasswordService, LoginLogService loginLogService) {
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.userService = userService;

        this.resetPasswordService = resetPasswordService;
        this.loginLogService = loginLogService;
    }

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        logger.info("AuthService: Create user");
        User user = userService.convertToEntity(userRequestDTO,encodePassword(userRequestDTO.password()));

        if(userService.existsByEmail(user.getEmail())){
            throw new UserAlreadyExistException("Email already registered. Please log in or recover your password.");
        }

        userService.saveUser(user);

        return new UserResponseDTO(user.getUsername(), user.getEmail(), tokenService.generateToken(user, loginLogService.saveLoginLog(user, ScopeToken.REGISTER_TOKEN, LocalDateTime.now())));
    }

    public LoginResponseDTO userLogin(LoginRequestDTO bodyRequest) {
        logger.info("AuthService: Login user");
        User user = userService.findByIdWithApplications(bodyRequest.email());

        if(Boolean.TRUE.equals(user.getBlockUser())){
            logger.info("AuthService: User blocl, throw exception");
            throw new UserBlockException("This user has been blocked. Use the password reset link.");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            logger.info("AuthService: User try login with OAuth, throw exception");
            throw new InvalidCredentialsException("Direct login is not allowed for users created via OAuth.");
        }

        if (validadeCredentials(bodyRequest.password(), user.getPassword())) {
            logger.info("AuthService: invalid credencials, throw exception");
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        logger.info("AuthService: Login success");
        return new LoginResponseDTO(user.getEmail(),
                tokenService.generateToken(user, loginLogService.saveLoginLog(user, ScopeToken.LOGIN_TOKEN, LocalDateTime.now())),
                tokenService.generateRefreshToken(user));
    }

    public String refreshAccessToken(String refreshToken) {
        logger.info("AuthService: Refresh Access Token");
        String email = tokenService.validateRefreshToken(refreshToken);
        if (email == null) {
            logger.info("AuthService: email is null, throw exception");
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
        User user = userService.findByIdWithApplications(email);
        LoginLogResponseDTO loginLogResponseDTO = loginLogService.saveLoginLog(user, ScopeToken.REFRESH_TOKEN, LocalDateTime.now());
        logger.info("AuthService: return refresh access token");
        return tokenService.generateToken(user, loginLogResponseDTO);
    }

    public void existsUserByEmail(String email) {
        logger.info("AuthService: Call UserService to check if the exists user by the email");
        if (userService.existsByEmail(email)) {
            logger.info("AuthService: email not exist, throw excpetion");
            throw new UserAlreadyExistException("Email already registered. Please log in or recover your password.");
        }
    }

    public String processOAuth2User(OAuth2User oAuth2User) {
        logger.info("AuthService: Process OAuth User");
        String email = oAuth2User.getAttribute("email");
        User user;
        if(userService.existsByEmail(email)){
            logger.info("AuthService: User exist, login with OAuth");
            user = userService.findByIdWithApplications(email);
            return tokenService.generateToken(user, loginLogService.saveLoginLog(user, ScopeToken.LOGIN_TOKEN, LocalDateTime.now()));
        }

        logger.info("AuthService: User not exist, create user of OAuth");
        user = createNewUserFromOAuth(oAuth2User);
        return tokenService.generateToken(user, loginLogService.saveLoginLog(user, ScopeToken.REGISTER_TOKEN, LocalDateTime.now()));
    }

    public void updatePassword(PasswordOperationRequestDTO request) {
        logger.info("AuthService: Update password");
        User user = userService.findByEmail(request.email());
        validateOldPassword(request.oldPassword(), user.getPassword());
        userService.updatePassword(user.getEmail(), user.getUserId(), passwordEncoder.encode(request.newPassword()), LocalDateTime.now());
    }

    public String forgotPassword(PasswordOperationRequestDTO request) {
        logger.info("AuthService: Forgot password");
        changeResetPassword(request.email());
        User user = userService.findByEmail(request.email());
        String recoverCode = generateRecoverCode();
        resetPasswordService.createResetPassword(user, encodePassword(recoverCode));
        sendEmail(user.getEmail(), buildResetEmailBody(user.getUsername(), recoverCode));
        logger.info("AuthService: send reset code to email");
        return "Password reset code sent to your email.";
    }

    public ResetPasswordResponseDTO validateRecoverCode(PasswordOperationRequestDTO requestRecoverCode) {
        logger.info("AuthService: validate recorver token");
        ResetPassword resetPassword = resetPasswordService.findByEmail(requestRecoverCode.email());
        resetPasswordService.validateTokenExpiration(requestRecoverCode.recoverCode());
        validateRecoverCodeValues(resetPassword, requestRecoverCode);
        return new ResetPasswordResponseDTO(tokenService.generateRecorverToken(resetPassword));
    }

    public String resetPassword(PasswordOperationRequestDTO passwordOperationRequestDTO) {
        logger.info("AuthService: Reset password");
        ResetPassword resetPassword = resetPasswordService.findByEmail(passwordOperationRequestDTO.email());
        User user = userService.findByEmail(resetPassword.getUserEmail());
        userService.updatePassword(user.getEmail(), user.getUserId(), passwordOperationRequestDTO.newPassword(), LocalDateTime.now());
        return "Password reset successfully";
    }

    public String generateRefreshToken(String email){
        logger.info("AuthService: Generate Refresh token");
        User user = userService.findByIdWithApplications(email);
        return tokenService.generateRefreshToken(user);
    }

    private void validateOldPassword(String oldPassword, String currentPassword) {
        logger.info("AuthService: validate old password");
        if (validadeCredentials(oldPassword, currentPassword)) {
            logger.info("AuthService: Invalid credentials, throw exception");
            throw new InvalidCredentialsException("Invalid credencials");
        }
    }

    private void validateRecoverCodeValues(ResetPassword resetPassword, PasswordOperationRequestDTO requestRecoverCode){
        logger.info("AuthService: Validate values of recover code");
        if(!passwordEncoder.matches(requestRecoverCode.recoverCode(), resetPassword.getRecoverCode())){
            logger.info("AuthService: recover code not match, throw exception");
            throw new InvalidRecorveCodeExcpetion("Invalid recovery code.");
        }

        if(resetPassword.getExpirationDate().isBefore(LocalDateTime.now())){
            logger.info("AuthService: Recover code has expired, throw exception");
            throw new InvalidRecorveCodeExcpetion("Recovery code has expired.");
        }
    }

    private String generateRecoverCode() {
        int code = random.nextInt(900000) + 100000; // Gera um número entre 100000 e 999999
        return String.valueOf(code);
    }

    private String buildResetEmailBody(String username, String recoverCode) {
        return String.format("<p>Hello %s,</p><p>Your password reset code is: <strong>%s</strong></p><p>This code will expire in 30 minutes.</p>", username, recoverCode);
    }

    private boolean validadeCredentials(String loginPassword, String userPassword) {
        return !passwordEncoder.matches(loginPassword, userPassword);
    }

    private void sendEmail(String email, String htmlBody) {
        logger.info("AuthService: Send email to recorver code");
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

    public User createNewUserFromOAuth(OAuth2User oAuth2User) {
        UserRequestDTO userRequestDTO = userService.convertOAuthUserToRequestDTO(oAuth2User);
        User newUser = userService.convertToEntity(userRequestDTO, encodePassword(""));
        userService.saveUser(newUser);
        return newUser;
    }

}