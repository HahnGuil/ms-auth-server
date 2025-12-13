package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.DirectLoginNotAllowedException;
import br.com.hahn.auth.application.execption.InvalidCredentialsException;
import br.com.hahn.auth.application.execption.InvalidTokenException;
import br.com.hahn.auth.application.execption.UserBlockException;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.model.*;
import br.com.hahn.auth.infrastructure.security.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    private final UserService userService;
    private final TokenLogService tokenLogService;
    private final TokenService tokenService;
    private final LoggedNowService loggedNowService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(@Lazy UserService userService, TokenLogService tokenLogService, TokenService tokenService, LoggedNowService loggedNowService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.tokenLogService = tokenLogService;
        this.tokenService = tokenService;
        this.loggedNowService = loggedNowService;
        this.passwordEncoder = passwordEncoder;
    }

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
        log.info("AuthService: Starting login on Login Service for user: {}, at: {}", loginRequest.getEmail(), Instant.now());

        log.info("AuthService: Validating the existence of the email: {}", loginRequest.getEmail());
        var user = userService.findByEmail(loginRequest.getEmail());

        log.info("AuthService: Checking if the user: {} is currently logged in at: {}.", user.getUserId(), Instant.now());
        validateIfUserIsAlreadyLoggedIn(user);

        log.info("AuthService: Validating if the user: {}, are OAuth user at: {}", user.getUserId(), Instant.now());
        validatingYourUserIsOauth(user);

        log.info("AuthService: Validating if the user: {} are block at: {}", user.getUserId(), Instant.now());
        validateBlockUser(user);

        log.info("Login Service: Validating credentials of the user: {} at: {}", user.getUserId(), Instant.now());
        validateCredentials(loginRequest.getPassword(), user.getPassword());

        return convertToLoginResponse(user);
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
        log.info("AuthService: Starting process Login or Register for OAuthUser with email: {},  at: {}", oAuth2User.getAttribute("email"), Instant.now());

        String email = oAuth2User.getAttribute("email");
        User user;

        if(userService.existsByEmail(email)){
            log.info("AuthService: OAuth User exist. Starting login for OAuthUser with email: {} at {}: ", email, Instant.now());
            user = userService.findByEmail(email);
        }else {
            log.info("AuthService: OAuth user not exist. Starting create user for OAuthRequest with email: {}, at: {}", email, Instant.now());
            user = userService.createNewUserFromOAuth(oAuth2User);
        }

        return convertToLoginResponse(user);
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
        log.info("AuthService: Starting log off for user with email: {}, at: {}", jwt.getSubject(), Instant.now());
        var userId = getUserIdFromToken(jwt);
        doLogOff(userId, TypeInvalidation.LOG_OFF);

    }

    /**
     * Performs the logoff process for a user.
     * <p>
     * This method executes the following steps:
     * - Deletes the user's active session from the `loggedNowService`.
     * - Deactivates the user's active token in the `tokenLogService` with the specified invalidation type.
     * </p>
     *
     * @author HahnGuil
     * @param userId the UUID of the user to log off
     * @param typeInvalidation the type of invalidation to apply to the user's token
     */
    public void doLogOff(UUID userId, TypeInvalidation typeInvalidation){
        log.info("AuthService: Execute user logOff for user: {}, with type: {} ,at: {}", userId, typeInvalidation.toString(), Instant.now());
        loggedNowService.deleteByUserId(userId);
        tokenLogService.deactivateActiveToken(userId, typeInvalidation);
    }

    /**
     * Validates the user's current (old) password.
     * Compares the provided oldPassword with the stored password of the given user.
     * If the passwords do not match, an InvalidCredentialsException is thrown.
     *
     * @author HahnGuil
     * @param user the User whose password will be validated
     * @param oldPassword the plain-text old password provided for validation
     * @throws InvalidCredentialsException if the provided oldPassword does not match the user's stored password
     */
    public void validateOldPassword(User user, String oldPassword){
        log.info("AuthService: Staring validating for oldPassword for user: {} at: {}", user.getUserId(), Instant.now());
        validateCredentials(oldPassword, user.getPassword());
    }

    /**
     * Generates a new token for the user based on the provided JWT.
     * This method performs the following steps:
     * - Extracts the token log ID from the JWT and validates if the refresh token has already been used.
     * - Extracts the user ID from the JWT and deactivates the current token.
     * - Generates a new access token and refresh token for the user.
     * - Saves the new token log and returns the LoginResponse.
     *
     * @author HahnGuil
     * @param jwt the JWT token containing user information and claims
     * @return LoginResponse containing the user's data and new tokens
     */
    public LoginResponse generateNewTokenForUser(Jwt jwt){
        log.info("AuthService: Starting generate new token for user email: {}, at: {}", jwt.getSubject(), Instant.now());


        log.info("AuthService: Extract token log id and validate the scope and expiration time for user: {}, already use for generate new token at: {}", jwt.getSubject(), Instant.now());
        String idToken = jwt.getClaim("token_log_id").toString();
        var tokenLogId = UUID.fromString(idToken);

        isRefreshToken(tokenLogId);
        checkTokenActive(tokenLogId);

        log.info("AuthService: Extract user id for Deactivate the actual token of user: {}, at: {}", jwt.getSubject(), Instant.now());
        String idUser = jwt.getClaim("user_id").toString();
        var userID = UUID.fromString(idUser);
        tokenLogService.deactivateActiveToken(userID, TypeInvalidation.USER_REFRESH);

        log.info("AuthService: Generated new access token for user: {}, at: {}", userID, Instant.now());
        var user = userService.findByEmail(jwt.getSubject());

        return convertToLoginResponse(user);
    }

    /**
     * Checks if the token associated with the given tokenLogId is still valid.
     * If the token is invalid, logs an error message and throws an InvalidCredentialsException.
     *
     * @author HahnGuil
     * @param tokenLogId the UUID of the token log to be validated
     * @throws InvalidCredentialsException if the token is no longer valid
     */
    private void checkTokenActive (UUID tokenLogId) {
        if (!tokenLogService.isTokenValid(tokenLogId)) {
            log.error("AuthService: Refresh token expired. Throw InvalidCredentialsException at: {}", Instant.now());
            throw new InvalidCredentialsException(ErrorsResponses.EXPIRED_REFRESH_TOKEN.getMessage());
        }
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
        String userId = jwt.getClaim("user_id").toString();
        return UUID.fromString(userId);
    }

    /**
     * Converts a User and LoginLog into a LoginResponse object.
     * This method generates tokens for the user, sets the attributes of the LoginResponse,
     * and returns the populated LoginResponse object.
     *
     * @author HahnGuil
     * @param user the User object containing user details
     * @return LoginResponse containing the user's name, email, token, and refresh token
     */
    private LoginResponse convertToLoginResponse(User user){
        log.info("AuthService: Generate token for user: {}, using token service at: {}", user.getUserId(), Instant.now());
        var tokenLogLogin = tokenLogService.saveTokenLog(user, ScopeToken.LOGIN_TOKEN, LocalDateTime.now());
        var token = tokenService.generateToken(user, tokenLogLogin);

        log.info("AuthService: Generate refreshToken for user: {}, using token service at: {}", user.getUserId(), Instant.now());
        var refreshTokenLogin = tokenLogService.saveTokenLog(user, ScopeToken.REFRESH_TOKEN, LocalDateTime.now());
        var refreshToken = tokenService.generateRefreshToken(user, refreshTokenLogin);

        log.info("AuthService: Setting loginResponse attributes for user: {}, at: {}", user.getUserId(), Instant.now());
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUserName(user.getFirstName() + user.getLastName());
        loginResponse.setEmail(user.getEmail());
        loginResponse.setToken(token);
        loginResponse.setRefreshToken(refreshToken);
        return loginResponse;
    }

    /**
     * Validate if the user is already logged in.
     * <p>
     * This method performs the following steps:
     * 1. Logs the start of the validation.
     * 2. Retrieves all LoggedNow entries for the given user.
     * 3. If the list is not empty:
     *    - Deletes all LoggedNow records for that user.
     *    - Deactivates the user's active token using TypeInvalidation.NEW_LOGIN.
     * 4. Otherwise, it logs that no active tokens/sessions were found and allows the flow to continue.
     * <p>
     * Note: This method does not throw an exception; it forces a new login by cleaning previous
     * session records and deactivating existing tokens.
     * <p>
     * @author HahnGuil
     * @param user the User object to check for active sessions
     */
    private void validateIfUserIsAlreadyLoggedIn(User user){
        log.info("AuthService: Find LoggedNow fot user: {}, at: {}", user.getUserId(), Instant.now());
        List<LoggedNow> logRegistersOfUser = loggedNowService.findByUserId(user.getUserId());

        if(!logRegistersOfUser.isEmpty()){
            log.info("AuthService: Find active session for the user: {}, starting do delete e deactivate the token at: {}", user.getUserId(), Instant.now());
            loggedNowService.deleteByUserId(user.getUserId());
            tokenLogService.deactivateActiveToken(user.getUserId(), TypeInvalidation.NEW_LOGIN);
        }

        log.info("AuthService: Token not foud or is null. User: {}, is not logged, finish validation at {}: ",user.getUserId(), Instant.now());
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
            log.error("AuthService: User block, throw exception");
            throw new UserBlockException(ErrorsResponses.USER_BLOCK.getMessage());
        }
    }

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
    private void validateCredentials(String loginPassword, String userPassword) {
        if(!passwordEncoder.matches(loginPassword, userPassword)){
            throw new InvalidCredentialsException(ErrorsResponses.INVALID_CREDENTIALS.getMessage());
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
    private void validatingYourUserIsOauth(User user){
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            log.error("AuthService: User: {} try login with OAuth, throw exception at {}", user.getUserId(), Instant.now());
            throw new DirectLoginNotAllowedException(ErrorsResponses.USER_OAUTH_CAN_NOT_LOGIN_DIRECT.getMessage());
        }
    }

    /**
     * Validates if the provided token has the "REFRESH_TOKEN" scope.
     * <p>
     * This method checks the scope of the given `TokenLog` object to ensure it is
     * a refresh token. If the token does not have the expected scope, an
     * `InvalidTokenException` is thrown with an appropriate error message.
     * </p>
     *
     * @author HahnGuil
     * @param tokenLogId the id of `TokenLog` the object to be validated
     * @throws InvalidTokenException if the token does not have the "REFRESH_TOKEN" scope
     */
    private void isRefreshToken(UUID tokenLogId){
        log.info("AuthService: validate if token: {}, have a Refresh Scope Token at: {}", tokenLogId, Instant.now());
        var tokenlog = tokenLogService.findById(tokenLogId);

        var scope = tokenlog.getScopeToken();

        if(!ScopeToken.REFRESH_TOKEN.equals(scope)){
            log.error("AuthService: Inform token dont have the expect scope token. Inform token is: {}, user of request is: {}. Throw InvalidTokenException at: {}", scope, tokenlog.getUserId(), Instant.now());
            throw new InvalidTokenException(ErrorsResponses.TOKEN_MUST_BE_REFRESH.getMessage() + scope);
        }
    }
}
