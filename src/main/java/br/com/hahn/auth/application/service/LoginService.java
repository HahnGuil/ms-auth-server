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
import org.springframework.security.oauth2.core.user.OAuth2User;
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


    /**
     * Method used to perform user login.
     * Receives the request from the controller and performs the following validations:
     * - Validates if the informed email exists
     * - Validates if the user is currently logged in
     * - Validates if the user is an OAuth user (These users can only log in via OAuth)
     * - Validates if the user is blocked
     * - Validates if the password is correct
     * After validations, returns the LoginResponse
     * @author HahnGuil
     * @param loginRequest the login request containing user credentials
     * @return LoginResponse containing user data and tokens
     */
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

    /**
     * Processes the login or registration of an OAuth user.
     * This method checks if the user already exists in the system:
     * - If the user exists, it logs them in and creates a login log.
     * - If the user does not exist, it creates a new user from the OAuth request and logs them in.
     *
     * @author HahnGuil
     * @param oAuth2User the OAuth2User object containing user details from the OAuth provider
     * @return LoginResponse containing user data and tokens
     */
    public LoginResponse processOAuthUser(OAuth2User oAuth2User){
        log.info("LoginService: Starting process Login or Register for OAuthUser with email: {},  at: {}", oAuth2User.getAttribute("email"), Instant.now());

        String email = oAuth2User.getAttribute("email");
        User user;
        LoginLog loginLog;

        if(userService.existsByEmail(email)){
            log.info("LoginService: OAuth User exist. Starting login for OAuthUser with email: {} at {}: ", email, Instant.now());
            user = userService.findByIdWithApplications(email);
            loginLog = loginLogService.saveLoginLog(user, ScopeToken.LOGIN_TOKEN, LocalDateTime.now());
        }else {
            log.info("LoginService: OAuth user not exist. Stargin create user for OAuthRequest with email: {}, at: {}", email, Instant.now());
            user = userService.createNewUserFromOAuth(oAuth2User);
            loginLog = loginLogService.saveLoginLog(user, ScopeToken.REGISTER_TOKEN, LocalDateTime.now());
        }

        return convertToLoginResponse(user, loginLog);
    }

    /**
     * Logs off the user by deactivating their active session and tokens.
     * This method performs the following steps:
     * - Retrieves the user ID from the provided JWT token.
     * - Deletes the user's active session from the loggedNowService.
     * - Deactivates the user's active token in the loginLogService.
     *
     * @author HahnGuil
     * @param jwt the JWT token containing user information
     */
    public void logOffUser(Jwt jwt){
        log.info("LoginService: Starting log off for user with email: {}, at: {}", jwt.getSubject(), Instant.now());
        var userId = getUserIdFromToken(jwt);
        loggedNowService.deleteByUserId(userId);
        loginLogService.deactivateActiveToken(userId, TypeInvalidation.LOG_OFF);
    }

    /**
     * Extracts the user ID from the provided JWT token.
     * This method retrieves the "user_id" claim from the JWT and converts it to a UUID.
     *
     * @author HahnGuil
     * @param jwt the JWT token containing the "user_id" claim
     * @return the user ID as a UUID
     */
    private UUID getUserIdFromToken(Jwt jwt){
        return UUID.fromString(jwt.getClaimAsString("user_id"));
    }

    /**
     * Converts a User and LoginLog into a LoginResponse object.
     * This method generates tokens for the user, sets the attributes of the LoginResponse,
     * and returns the populated LoginResponse object.
     *
     * @author HahnGuil
     * @param user the User object containing user details
     * @param loginLog the LoginLog object containing login details
     * @return LoginResponse containing the user's name, email, token, and refresh token
     */
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

    /**
     * Validates if the user is already logged in.
     * This method checks if there is an active session for the given user.
     * If the user is already logged in, it throws a UserAlreadyLoggedInException.
     *
     * @author HahnGuil
     * @param user the User object to check for an active session
     * @throws UserAlreadyLoggedInException if the user is already logged in
     */
    private void validateIfUserIsAlreadyLoggedIn(User user){
        if(loggedNowService.existsByUserId(user.getUserId())){
            throw new UserAlreadyLoggedInException("The user is already logged in at this time.");
        }
    }

    /**
     * Validates if the user is blocked.
     * This method checks if the user's `blockUser` attribute is set to `true`.
     * If the user is blocked, a `UserBlockException` is thrown.
     *
     * @author HahnGuil
     * @param user the User object to be validated
     * @throws UserBlockException if the user is blocked
     */
    private void validateBlockUser(User user){
        if(Boolean.TRUE.equals(user.getBlockUser())){
            log.error("AuthService: User blocl, throw exception");
            throw new UserBlockException("This user has been blocked. Use the password reset link.");
        }
    }
}
