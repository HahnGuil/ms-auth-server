package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.UserAlreadyLoggedInException;
import br.com.hahn.auth.application.execption.UserBlockException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.model.LoginLog;
import br.com.hahn.auth.domain.model.LoginRequest;
import br.com.hahn.auth.domain.model.LoginResponse;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.infrastructure.security.TokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class LoginService {

    private final UserService userService;
    private final AuthService authService;
    private final LoginLogService loginLogService;
    private final TokenService tokenService;
    private final LoggedNowService loggedNowService;


    public LoginResponse userLogin(LoginRequest loginRequest){
        log.info("LoginService: Starting loing on Login Service for user: {}, at: {}", loginRequest.getEmail(), Instant.now());

        log.info("LoginService: Validating the existence of the email: {}", loginRequest.getEmail());
        var user = userService.findByEmail(loginRequest.getEmail());

        log.info("LoginService: Checking if the user: {} is currently logged in at: {}.", user.getUserId(), Instant.now());
        validateIfUserIsAlreadyLoggedIn(user);

        log.info("LoginService: Validating if the user: {}, are OAuth user at: {}", user.getUserId(), Instant.now());
        authService.validatingYourUserIsOauth(user);

        log.info("LoginService: Validating if the user: {} are block at: {}", user.getUserId(), Instant.now());
        validateBlockUser(user);

        log.info("Login Service: Validating credentials of the user: {} at: {}", user.getUserId(), Instant.now());
        authService.validadeCredentials(loginRequest.getPassword(), user.getPassword());

        var loginLog = loginLogService.saveLoginLog(user, ScopeToken.LOGIN_TOKEN, LocalDateTime.now());

        return convertToLoginResponse(user, loginLog);
    }

    public void logOffUser(Jwt jwt){
        log.info("LoginService: Starting log off for user with email: {}, at: {}", jwt.getSubject(), Instant.now());
        var userId = getUserIdFromToken(jwt);
        loggedNowService.deleteByUserId(userId);
        loginLogService.deactivateActiveToken(userId, TypeInvalidation.LOG_OFF);
    }

    private UUID getUserIdFromToken(Jwt jwt){
        return UUID.fromString(jwt.getClaimAsString("user_id"));
    }

    private LoginResponse convertToLoginResponse(User user, LoginLog loginLog){
        log.info("LoginService: Gerenate token for user: {}, using token service at: {}", user.getUserId(), Instant.now());
        var token = tokenService.generateToken(user, loginLog);

        log.info("LoginService: Generate refreshToken for user: {}, using token service at: {}", user.getUserId(), Instant.now());
        var refreshToken = tokenService.generateRefreshToken(user, loginLog);

        log.info("LoginService: Setting loginResponse atributes for user: {}, at: {}", user.getUserId(), Instant.now());
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUserName(user.getFirstName() + user.getLastName());
        loginResponse.setEmail(user.getEmail());
        loginResponse.setToken(token);
        loginResponse.setRefreshToken(refreshToken);
        return loginResponse;
    }

    private void validateIfUserIsAlreadyLoggedIn(User user){
        if(loggedNowService.existsByUserId(user.getUserId())){
            throw new UserAlreadyLoggedInException("The user is already logged in at this time.");
        }
    }

    private void validateBlockUser(User user){
        if(Boolean.TRUE.equals(user.getBlockUser())){
            log.error("AuthService: User blocl, throw exception");
            throw new UserBlockException("This user has been blocked. Use the password reset link.");
        }
    }
}
