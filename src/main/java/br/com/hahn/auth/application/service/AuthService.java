package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.request.*;
import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.dto.response.ResetPasswordResponseDTO;
import br.com.hahn.auth.application.dto.response.UserResponseDTO;
import br.com.hahn.auth.application.execption.InvalidCredentialsException;
import br.com.hahn.auth.application.execption.InvalidOperationExecption;
import br.com.hahn.auth.application.execption.ResourceAlreadyExistException;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.ResetPasswordRepository;
import br.com.hahn.auth.domain.respository.UserRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import br.com.hahn.auth.infrastructure.service.EmailService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final ResetPasswordRepository resetPasswordRepository;

    private final Random random = new Random();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService, EmailService emailService, ResetPasswordRepository resetPasswordRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.resetPasswordRepository = resetPasswordRepository;
    }

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        User user = convertToEntity(userRequestDTO);
        saveUser(user);
        return new UserResponseDTO(user.getUsername(), user.getEmail());
    }

    public LoginResponseDTO userlogin(LoginRequestDTO bodyRequest) {
        User user = findByEmail(bodyRequest.email());

        if (!validadeCredentials(bodyRequest.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Email or password invalid");
        }

        return new LoginResponseDTO(user.getEmail(), this.tokenService.generateToken(user), this.tokenService.generateRefreshToken(user));
    }

    public String generateRefreshToken(String email){
        User user = findByEmail(email);
        return tokenService.generateRefreshToken(user);
    }

    public String refreshAccessToken(String refreshToken) {
        String email = tokenService.validateRefreshToken(refreshToken);
        if (email == null) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }
        User user = findByEmail(email);
        return tokenService.generateToken(user);
    }

    public void existsUserByEmail(String email) {
        if(userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistException("User already exists with email, please use other or try to recover your password");
        }
    }

    public User convertToEntity(UserRequestDTO userRequestDTO) {

        User user = new User();
        user.setUsername(userRequestDTO.userName());
        user.setPassword(encodePassword(userRequestDTO.password()));
        user.setEmail(userRequestDTO.email());
        user.setFirstName(userRequestDTO.firstName());
        user.setLastName(userRequestDTO.lastName());
        user.setPictureUrl(userRequestDTO.pictureUrl());

        return user;
    }

    public String processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUserFromOAuth(oAuth2User));
        return generateTokenForUser(user);
    }

    public void updatePassword(ChangePasswordRequestDTO changePasswordRequestDTO) {
        User user = findByEmail(changePasswordRequestDTO.email());

        if (validadeCredentials(changePasswordRequestDTO.oldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Old password is invalid");
        }

        user.setPassword(encodePassword(changePasswordRequestDTO.newPassword()));

        try {
            userRepository.save(user);
        } catch (Exception _) {
            throw new InvalidOperationExecption("Operation not allowed, please try again later");
        }
    }

    public String forgotPassword(RequestForgotPasswordDTO requestForgotPasswordDTO) {
        checkExistingResetToken(requestForgotPasswordDTO);
        User user = findByEmail(requestForgotPasswordDTO.email());
        String recoverCode = gerenateRecoverCode();

        try {
            ResetPassword resetPassword = createResetPassword(user, recoverCode);
            resetPasswordRepository.save(resetPassword);

            String subject = "Password Reset Request";
            String htmlBody = String.format("<p>Hello %s,</p><p>Your password reset code is: <strong>%s</strong></p><p>This code will expire in 30 minutes.</p>",
                    user.getUsername(), recoverCode);

            sendEmail(user.getEmail(), subject, htmlBody);
        } catch (DataAccessException _) {
            throw new InvalidOperationExecption("Operation not allowed, please try again later.");
        }

        return "Password reset code sent to your email.";
    }

    public ResetPasswordResponseDTO checkRecoverCode(ResetPasswordRequestDTO resetPasswordRequestDTO){
        ResetPassword resetPassword = resetPasswordRepository.findByUserEmail(resetPasswordRequestDTO.email()).orElseThrow(() -> new InvalidCredentialsException("Invalid Recorver Code for this email"));

        if (!validateRecoverRequest(resetPassword, resetPasswordRequestDTO)) {
            throw new InvalidCredentialsException("Recorve code expired or invalid.");
        }

        String recoverToken = tokenService.generateRecorverToken(resetPassword);

        resetPasswordRepository.deleteById(resetPassword.getId());

        return new ResetPasswordResponseDTO(recoverToken);
    }

    private void checkExistingResetToken(RequestForgotPasswordDTO requestForgotPasswordDTO) {
        Optional<ResetPassword> existingResetPassword = resetPasswordRepository.findByUserEmail(requestForgotPasswordDTO.email());
        if (existingResetPassword.isPresent()) {
            ResetPassword resetPassword = existingResetPassword.get();
            if (isExpirationDateValid(resetPassword.getExpirationDate())) {
                throw new ResourceAlreadyExistException("A code already exists for this email");
            }
        }
    }

    private boolean validateRecoverRequest(ResetPassword resetPassword, ResetPasswordRequestDTO resetPasswordRequestDTO) {
        return isExpirationDateValid(resetPassword.getExpirationDate())
                && validadeCredentials(resetPasswordRequestDTO.recorverCode(), resetPassword.getRecoverCode());
    }

    private ResetPassword createResetPassword(User user, String recorverCode){
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setRecoverCode(passwordEncoder.encode(recorverCode));
        resetPassword.setUserEmail(user.getEmail());
        resetPassword.setExpirationDate(generateExpirationDate());

        return resetPassword;
    }

    private boolean isExpirationDateValid(LocalDateTime expirationDate) {
        LocalDateTime now = LocalDateTime.now();
        return Duration.between(now, expirationDate).toMinutes() <= 30;
    }

    private boolean validadeCredentials(String loginPassword, String userPassword) {
        return passwordEncoder.matches(loginPassword, userPassword);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceAlreadyExistException("User not found. Check email and password or register a new user."));
    }

    private void saveUser(User user) {
        userRepository.save(user);
    }

    private void sendEmail(String email, String subject, String htmlBody) {
        try {
            emailService.enviarEmail(email, subject, htmlBody).block();
        } catch (Exception _) {
            throw new InvalidOperationExecption("Failed to send email. Please try again later.");
        }
    }

    private String gerenateRecoverCode() {
        Integer code = random.nextInt(999999 - 100000 + 1) + 100000;
        return String.valueOf(code);
    }

    private LocalDateTime generateExpirationDate() {
        return LocalDateTime.now().plusMinutes(30);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private String generateTokenForUser(User user) {
        return tokenService.generateToken(user);
    }

    private User createNewUserFromOAuth(OAuth2User oAuth2User) {
        UserRequestDTO userRequestDTO = convertOAuthUserToRequestDTO(oAuth2User);
        User newUser = convertToEntity(userRequestDTO);
        saveUser(newUser);
        return newUser;
    }

    private UserRequestDTO convertOAuthUserToRequestDTO(OAuth2User oAuth2User){
        return new UserRequestDTO(
                oAuth2User.getAttribute("name"),
                oAuth2User.getAttribute("email"),
                "",
                oAuth2User.getAttribute("given_name"), // First name
                oAuth2User.getAttribute("family_name"), // Last name
                oAuth2User.getAttribute("picture") // Picture URL
        );
    }
}
