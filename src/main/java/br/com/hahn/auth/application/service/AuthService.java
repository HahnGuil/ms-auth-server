package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.execption.DirectLoginNotAllowedException;
import br.com.hahn.auth.application.execption.InvalidCredentialsException;
import br.com.hahn.auth.application.execption.UserAlreadyLoggedInException;
import br.com.hahn.auth.application.execption.UserBlockException;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.model.LoginRequest;
import br.com.hahn.auth.domain.model.LoginResponse;
import br.com.hahn.auth.domain.model.TokenLog;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.infrastructure.security.TokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import javax.print.DocFlavor;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final TokenLogService tokenLogService;
    private final TokenService tokenService;
    private final LoggedNowService loggedNowService;
    private final PasswordEncoder passwordEncoder;


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
        log.info("AuthService: Starting loing on Login Service for user: {}, at: {}", loginRequest.getEmail(), Instant.now());

        log.info("AuthService: Validating the existence of the email: {}", loginRequest.getEmail());
        var user = userService.findByEmail(loginRequest.getEmail());

        log.info("AuthService: Checking if the user: {} is currently logged in at: {}.", user.getUserId(), Instant.now());
        validateIfUserIsAlreadyLoggedIn(user);

        log.info("AuthService: Validating if the user: {}, are OAuth user at: {}", user.getUserId(), Instant.now());
        validatingYourUserIsOauth(user);

        log.info("AuthService: Validating if the user: {} are block at: {}", user.getUserId(), Instant.now());
        validateBlockUser(user);

        log.info("Login Service: Validating credentials of the user: {} at: {}", user.getUserId(), Instant.now());
        validadeCredentials(loginRequest.getPassword(), user.getPassword());

        var loginLog = tokenLogService.saveTokenLog(user, ScopeToken.LOGIN_TOKEN, LocalDateTime.now());

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
        log.info("AuthService: Starting process Login or Register for OAuthUser with email: {},  at: {}", oAuth2User.getAttribute("email"), Instant.now());

        String email = oAuth2User.getAttribute("email");
        User user;
        TokenLog tokenLog;

        if(userService.existsByEmail(email)){
            log.info("AuthService: OAuth User exist. Starting login for OAuthUser with email: {} at {}: ", email, Instant.now());
            user = userService.findByEmail(email);
            tokenLog = tokenLogService.saveTokenLog(user, ScopeToken.LOGIN_TOKEN, LocalDateTime.now());
        }else {
            log.info("AuthService: OAuth user not exist. Stargin create user for OAuthRequest with email: {}, at: {}", email, Instant.now());
            user = userService.createNewUserFromOAuth(oAuth2User);
            tokenLog = tokenLogService.saveTokenLog(user, ScopeToken.REGISTER_TOKEN, LocalDateTime.now());
        }

        return convertToLoginResponse(user, tokenLog);
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
        loggedNowService.deleteByUserId(userId);
        tokenLogService.deactivateActiveToken(userId, TypeInvalidation.LOG_OFF);
    }

    public void validateOldPassword(User user, String oldPassword){
        log.info("AuthService: Staring validating for oldPassword for user: {} at: {}", user.getUserId(), Instant.now());
        validadeCredentials(oldPassword, user.getPassword());
    }
    
    
    public LoginResponse generateNewTokenForUser(Jwt jwt){
        log.info("AuthService: Starting generate new token for user email: {}, at: {}", jwt.getSubject(), Instant.now());

        log.info("AuthService: Extract token log id for validate if the refresh token of user: {}, already use for generate new token at: {}", jwt.getSubject(), Instant.now());
        var tokenLogId = UUID.fromString(jwt.getClaim("token_log_id"));
        checkTokenActive(tokenLogId);

        log.info("AuthService: Extract user id for Deactivate the actual token of user: {}, at: {}", jwt.getSubject(), Instant.now());
        var userID = UUID.fromString(jwt.getClaim("user_id"));
        tokenLogService.deactivateActiveToken(userID, TypeInvalidation.USER_REFRESH);

        log.info("AuthService: Generated new access token for user: {}, at: {}", userID, Instant.now());
        var user = userService.findByEmail(jwt.getSubject());
        var tokenLog = tokenLogService.saveTokenLog(user, ScopeToken.LOGIN_TOKEN, LocalDateTime.now());

        return convertToLoginResponse(user, tokenLog);
    }

    private void checkTokenActive (UUID loginLogId) {
        if (!tokenLogService.isTokenValid(loginLogId)) {
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
        return UUID.fromString(jwt.getClaimAsString("user_id"));
    }

    /**
     * Converts a User and LoginLog into a LoginResponse object.
     * This method generates tokens for the user, sets the attributes of the LoginResponse,
     * and returns the populated LoginResponse object.
     *
     * @author HahnGuil
     * @param user the User object containing user details
     * @param tokenLog the LoginLog object containing login details
     * @return LoginResponse containing the user's name, email, token, and refresh token
     */
    private LoginResponse convertToLoginResponse(User user, TokenLog tokenLog){
        log.info("AuthService: Gerenate token for user: {}, using token service at: {}", user.getUserId(), Instant.now());
        var token = tokenService.generateToken(user, tokenLog);

        log.info("AuthService: Generate refreshToken for user: {}, using token service at: {}", user.getUserId(), Instant.now());
        var refreshToken = tokenService.generateRefreshToken(user, tokenLog);

        log.info("AuthService: Setting loginResponse atributes for user: {}, at: {}", user.getUserId(), Instant.now());
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
            throw new UserAlreadyLoggedInException(ErrorsResponses.USER_ALREADY_LOGGED.getMessage());
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
    private void validadeCredentials(String loginPassword, String userPassword) {
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
    private void validatingYourUserIsOauth(User user){
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            log.error("AuthService: User: {} try login with OAuth, throw exception at {}", user.getUserId(), Instant.now());
            throw new DirectLoginNotAllowedException("OAuth users cannot log in directly.");
        }
    }
}
