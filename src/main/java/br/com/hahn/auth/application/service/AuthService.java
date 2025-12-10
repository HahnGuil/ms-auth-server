package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.request.PasswordOperationRequestDTO;
import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.execption.*;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.model.ResetPassword;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.infrastructure.security.TokenService;
import br.com.hahn.auth.infrastructure.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserService userService;
    private final TokenLogService tokenLogService;

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

    public void validateOldPassword(User user, String oldPassword){
        log.info("AuthService: Staring validating for oldPassword for user: {} at: {}", user.getUserId(), Instant.now());
         validadeCredentials(oldPassword, user.getPassword());
    }



//    ---------------------------
// Código Antigo

    public LoginResponseDTO refreshAccessToken(String token) {
        log.info("AuthService: Validate Refresh Token and get email from user");
        var user = userService.findByEmail(tokenService.validateToken(token));

        log.info("AuthService: Check if token already use");
        this.checkTokenActive(tokenService.extracLoginLogId(token));

        tokenLogService.deactivateActiveToken(user.getUserId(), TypeInvalidation.USER_REFRESH);

        log.info("AuthService: return renewed access token");
        var loginLog = tokenLogService.saveTokenLog(user, ScopeToken.LOGIN_TOKEN, LocalDateTime.now());

        return new LoginResponseDTO(user.getFirstName(),
                user.getEmail(),
                tokenService.generateToken(user, loginLog),
                tokenService.generateRefreshToken(user, loginLog));
    }

    private void checkTokenActive (UUID loginLogId){
        if(!tokenLogService.isTokenValid(loginLogId)){
            throw new InvalidCredentialsException("Token expired, need to log in again");
        }
    }
}