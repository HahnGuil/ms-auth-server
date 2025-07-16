package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.request.*;
import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.dto.response.ResetPasswordResponseDTO;
import br.com.hahn.auth.application.dto.response.UserResponseDTO;
import br.com.hahn.auth.application.execption.*;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.ResetPasswordRepository;
import br.com.hahn.auth.domain.respository.UserRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import br.com.hahn.auth.infrastructure.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private final Random random = new Random();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final ResetPasswordRepository resetPasswordRepository;


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

    public LoginResponseDTO userLogin(LoginRequestDTO bodyRequest) {
        User user = findByEmail(bodyRequest.email());

        if (!validadeCredentials(bodyRequest.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Email or password invalid");
        }

        return new LoginResponseDTO(user.getEmail(), tokenService.generateToken(user), tokenService.generateRefreshToken(user));
    }

    public String generateRefreshToken(String email){
        User user = findByEmail(email);
        return tokenService.generateRefreshToken(user);
    }

    public String refreshAccessToken(String refreshToken) {
        String email = tokenService.validateRefreshToken(refreshToken);
        if (email == null) {
            throw new InvalidRefreshTokenException("Invalid refresh token");

        }
        User user = findByEmail(email);
        return tokenService.generateToken(user);
    }

    public void existsUserByEmail(String email) {
        if(userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistException("User already exists with email, please use other or try to recover your password");
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

    public void updatePassword(PasswordOperationRequestDTO request) {
        User user = findByEmail(request.email());

        if (validadeCredentials(request.oldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Password is ivalid");
        }

        user.setPassword(encodePassword(request.newPassword()));
        saveUser(user);
    }

    public String forgotPassword(PasswordOperationRequestDTO request) {
        checkExistingResetToken(request.email());
        User user = findByEmail(request.email());
        String recoverCode = generateRecoverCode();
        saveResetPassword(user, recoverCode);
        sendEmail(user.getEmail(), buildResetEmailBody(user.getUsername(), recoverCode));
        return "Password reset code sent to your email.";
    }

    public ResetPasswordResponseDTO resetPassword(PasswordOperationRequestDTO request) {
        ResetPassword resetPassword = findResetPasswordByEmail(request.email());
        validateRecoverCode(resetPassword, request.recoverCode());
        resetPasswordRepository.deleteById(resetPassword.getId());
        User user = findByEmail(resetPassword.getUserEmail());
        return new ResetPasswordResponseDTO(tokenService.generateToken(user));
    }

    private void validateRecoverCode(ResetPassword resetPassword, String recoverCode) {
        if (!passwordEncoder.matches(recoverCode, resetPassword.getRecoverCode())) {
            throw new InvalidCredentialsException("Invalid recover code.");
        }
        if (resetPassword.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new InvalidOperationExecption("Recover code has expired.");
        }
    }

    private void saveResetPassword(User user, String recoverCode) {
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setRecoverCode(passwordEncoder.encode(recoverCode));
        resetPassword.setUserEmail(user.getEmail());
        resetPassword.setExpirationDate(LocalDateTime.now().plusMinutes(30));
        resetPasswordRepository.save(resetPassword);
    }

    private ResetPassword findResetPasswordByEmail(String email) {
        return resetPasswordRepository.findByUserEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid recover code for this email."));
    }

    private void checkExistingResetToken(String email) {
        resetPasswordRepository.findByUserEmail(email).ifPresent(resetPassword -> {
            if (resetPassword.getExpirationDate().isAfter(LocalDateTime.now())) {
                throw new ResourceAlreadyExistException("A code already exists for this email.");
            }
        });
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

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceAlreadyExistException("User not found. Check email and password or register a new user."));
    }

    private void saveUser(User user) {
        userRepository.save(user);
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