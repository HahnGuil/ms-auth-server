package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.request.*;
import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.dto.response.ResetPasswordResponseDTO;
import br.com.hahn.auth.application.dto.response.UserResponseDTO;
import br.com.hahn.auth.application.execption.*;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.infrastructure.security.TokenService;
import br.com.hahn.auth.infrastructure.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private final Random random = new Random();

    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final UserService userService;
    private final ResetPasswordService resetPasswordService;


    public AuthService(PasswordEncoder passwordEncoder, TokenService tokenService, EmailService emailService, UserService userService, ResetPasswordService resetPasswordService) {
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.userService = userService;

        this.resetPasswordService = resetPasswordService;
    }

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        User user = userService.convertToEntity(userRequestDTO,encodePassword(userRequestDTO.password()));

        if(userService.existsByEmail(user.getEmail())){
            throw new UserAlreadyExistException("Email already registered. Please log in or recover your password.");
        }

        userService.saveUser(user);
        return new UserResponseDTO(user.getUsername(), user.getEmail());
    }

    public LoginResponseDTO userLogin(LoginRequestDTO bodyRequest) {
        User user = userService.findByEmail(bodyRequest.email());

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new InvalidCredentialsException("Direct login is not allowed for users created via OAuth.");
        }

        if (!validadeCredentials(bodyRequest.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        return new LoginResponseDTO(user.getEmail(), tokenService.generateToken(user), tokenService.generateRefreshToken(user));
    }

    public String generateRefreshToken(String email){
        User user = userService.findByEmail(email);
        return tokenService.generateRefreshToken(user);
    }

    public String refreshAccessToken(String refreshToken) {
        String email = tokenService.validateRefreshToken(refreshToken);
        if (email == null) {
            throw new InvalidRefreshTokenException("Invalid refresh token");

        }
        User user = userService.findByEmail(email);
        return tokenService.generateToken(user);
    }

    public void existsUserByEmail(String email) {
        if (userService.existsByEmail(email)) {
            throw new UserAlreadyExistException("Email already registered. Please log in or recover your password.");
        }
    }

    public String processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        User user;
        try {
            user = userService.findByEmail(email);
        } catch (UserNotFoundException _) {
            user = createNewUserFromOAuth(oAuth2User);
        }
        return generateTokenForUser(user);
    }

    public void updatePassword(PasswordOperationRequestDTO request) {
        User user = userService.findByEmail(request.email());
        validateOldPassword(request.oldPassword(), user.getPassword());
        updateUserPassword(user.getEmail(), user.getUserId(), request.newPassword());
    }

    public String forgotPassword(PasswordOperationRequestDTO request) {
        changeResetPassword(request.email());
        User user = userService.findByEmail(request.email());
        String recoverCode = generateRecoverCode();
        resetPasswordService.createResetPassword(user, encodePassword(recoverCode));
        sendEmail(user.getEmail(), buildResetEmailBody(user.getUsername(), recoverCode));
        return "Password reset code sent to your email.";
    }

    public ResetPasswordResponseDTO validateRecoverCode(PasswordOperationRequestDTO requestRecoverCode) {
        ResetPassword resetPassword = resetPasswordService.findByEmail(requestRecoverCode.email());
        resetPasswordService.validateTokenExpiration(requestRecoverCode.recoverCode());
        validateRecoverCodeValues(resetPassword, requestRecoverCode);
        return new ResetPasswordResponseDTO(tokenService.generateRecorverToken(resetPassword));
    }

    public String resetPassword(PasswordOperationRequestDTO passwordOperationRequestDTO) {
        ResetPassword resetPassword = resetPasswordService.findByEmail(passwordOperationRequestDTO.email());
        User user = userService.findByEmail(resetPassword.getUserEmail());
        updateUserPassword(user.getEmail(), user.getUserId(), passwordOperationRequestDTO.newPassword());
        return "Password reset successfully";
    }

    private void validateOldPassword(String oldPassword, String currentPassword) {
        if (validadeCredentials(oldPassword, currentPassword)) {
            throw new InvalidCredentialsException("Invalid password.");
        }
    }

   private void updateUserPassword(String email, UUID id, String newPassword){
        userService.updatePassword(email, id, newPassword);
   }

    private void validateRecoverCodeValues(ResetPassword resetPassword, PasswordOperationRequestDTO requestRecoverCode){
        if(!passwordEncoder.matches(requestRecoverCode.recoverCode(), resetPassword.getRecoverCode())){
            throw new InvalidRecorveCodeExcpetion("Invalid recovery code.");
        }

        if(resetPassword.getExpirationDate().isBefore(LocalDateTime.now())){
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
        return passwordEncoder.matches(loginPassword, userPassword);
    }

    private void sendEmail(String email, String htmlBody) {
        try {
            emailService.enviarEmail(email, "Password Reset Request", htmlBody).block();
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

    private String generateTokenForUser(User user) {
        return tokenService.generateToken(user);
    }

    private User createNewUserFromOAuth(OAuth2User oAuth2User) {
        UserRequestDTO userRequestDTO = userService.convertOAuthUserToRequestDTO(oAuth2User);
        User newUser = userService.convertToEntity(userRequestDTO, encodePassword(""));
        userService.saveUser(newUser);
        return newUser;
    }

}