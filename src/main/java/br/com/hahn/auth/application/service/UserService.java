package br.com.hahn.auth.application.service;

import java.time.ZoneId;
import br.com.hahn.auth.application.execption.UserEmailAlreadyExistException;
import br.com.hahn.auth.application.execption.UserNotFoundException;
import br.com.hahn.auth.application.execption.InvalidFormatTypeException;
import br.com.hahn.auth.application.execption.ResourceAlreadyExistException;
import br.com.hahn.auth.domain.enums.*;
import br.com.hahn.auth.domain.model.*;
import br.com.hahn.auth.application.execption.ForbiddenOperationException;
import br.com.hahn.auth.domain.respository.PasswordParametersRepository;
import br.com.hahn.auth.domain.respository.PasswordPolicyRepository;
import br.com.hahn.auth.domain.respository.UserRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import br.com.hahn.auth.infrastructure.service.UserDataClient;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UserService {


    private final UserRepository userRepository;
    private final ApplicationService applicationService;
    private final TokenLogService tokenLogService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserDataService userDataService;
    private final PasswordParametersRepository passwordParametersRepository;
    private final PasswordPolicyRepository passwordPolicyRepository;

    public UserService(UserRepository userRepository, PasswordParametersRepository passwordParametersRepository, PasswordPolicyRepository passwordPolicyRepository, ApplicationService applicationService, @Lazy TokenLogService tokenLogService, TokenService tokenService, PasswordEncoder passwordEncoder, UserDataService userDataService) {
        this.userRepository = userRepository;
        this.passwordParametersRepository = passwordParametersRepository;
        this.passwordPolicyRepository = passwordPolicyRepository;
        this.applicationService = applicationService;
        this.tokenLogService = tokenLogService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.userDataService = userDataService;
    }


    /**
     * Creates a new user based on the provided user request.
     * This method performs the following steps:
     * - Logs the start of the user creation process.
     * - If the email already exists, registers the existing user in the requested application,
     *   unless it is already registered there.
     * - Otherwise creates the User entity.
     * - Saves the encoded password in a PasswordParameters bound to the user and the application.
     * - Converts the saved user entity into a UserResponse object and returns it.
     *
     * @author HahnGuil
     * @param userRequest the request object containing user details
     * @return UserResponse containing the details of the created user
     * @throws UserEmailAlreadyExistException if the email is already registered in the requested application
     */
    @Transactional
    public UserResponse createUser(UserRequest userRequest){
        log.info("UserService: Starting user creation for user email: {} at: {}", userRequest.getEmail(), DateTimeConverter.formatInstantNow());

        var application = applicationService.findById(userRequest.getApplicationCode());
        var encodedPassword = passwordEncoder.encode(userRequest.getPassword());

        var existingUser = userRepository.findByEmailWithApplications(userRequest.getEmail());
        if(existingUser.isPresent()){
            return registerExistingUserInApplication(existingUser.get(), application, encodedPassword);
        }

        log.info("UserService: Starting convert user with email: {} to entity at: {}", userRequest.getEmail(), DateTimeConverter.formatInstantNow());
        var user = convertToEntity(userRequest);
        userRepository.save(user);
        savePasswordParameters(user, application, encodedPassword);
        return convertToUserResponse(user, application);
    }

    /**
     * Registers an already existing user (same email) in another application.
     * The email can be reused across applications, but not twice in the same one.
     * The password is independent per application, so the informed password is stored only
     * for the requested application and nothing is checked against the other applications.
     */
    private UserResponse registerExistingUserInApplication(User user, Application application, String encodedPassword){
        var alreadyRegistered = TypeUser.OAUTH_USER.equals(user.getTypeUser())
                || passwordParametersRepository.existsByUserUserIdAndApplicationId(user.getUserId(), application.getId());
        if(alreadyRegistered){
            log.error("UserService: Email already registered for application: {}. throw UserEmailAlreadyExistsException at: {}", application.getId(), DateTimeConverter.formatInstantNow());
            throw new UserEmailAlreadyExistException(ErrorsResponses.EMAIL_ALREADY_REGISTER_ERROR.getMessage());
        }

        var applications = user.getApplications() == null ? new HashSet<Application>() : new HashSet<>(user.getApplications());
        applications.add(application);
        user.setApplications(applications);
        userRepository.save(user);
        savePasswordParameters(user, application, encodedPassword);
        return convertToUserResponse(user, application);
    }

    private void savePasswordParameters(User user, Application application, String encodedPassword){
        var parameters = new PasswordParameters();
        parameters.setUser(user);
        parameters.setApplication(application);
        parameters.setPassword(encodedPassword);
        parameters.setPasswordCreateDate(LocalDateTime.now(ZoneId.systemDefault()));
        parameters.setBlockUser(false);
        passwordParametersRepository.save(parameters);
    }

    /**
     * Finds the password parameters of the user for the given application.
     *
     * @param user the user
     * @param applicationPublicId the public id of the application
     * @return the password parameters of the pair user/application
     * @throws UserNotFoundException if the user is not registered with a password in that application
     */
    public PasswordParameters findPasswordParameters(User user, UUID applicationPublicId){
        return passwordParametersRepository.findByUserAndApplicationPublicId(user.getUserId(), applicationPublicId)
                .orElseThrow(() -> {
                    log.error("UserService: User: {} has no password for application: {}. Throw UserNotFoundException at: {}", user.getUserId(), applicationPublicId, DateTimeConverter.formatInstantNow());
                    return new UserNotFoundException(ErrorsResponses.USER_NOT_FOUD.getMessage());
                });
    }

    /**
     * Checks if a user with the given email exists in the repository.
     * This method performs the following steps:
     * - Logs the start of the email existence check process.
     * - Queries the repository to determine if the email exists.
     *
     * @author HahnGuil
     * @param email the email address to check for existence
     * @return true if the email exists, false otherwise
     */
    public boolean existsByEmail(String email){
        log.info("UserService: Checking if the requested email exists. For email {} at {}", email,  DateTimeConverter.formatInstantNow());
        return userRepository.existsByEmail(email);
    }

    /**
     * Retrieves a user by their email address.
     * This method performs the following steps:
     * - Logs the start of the user search process.
     * - Attempts to find the user along with their associated applications in the repository.
     * - If the user is not found, logs the error and throws a UserNotFoundException.
     *
     * @author HahnGuil
     * @param email the email address of the user to search for
     * @return the User object associated with the provided email
     * @throws UserNotFoundException if no user is found for the given email
     */
    public User findByEmail(String email){
        log.info("UserService: Searching for user for email: {} at: {}", email, DateTimeConverter.formatInstantNow());
        return userRepository.findByEmailWithApplications(email)
                .orElseThrow(() -> {
                    log.error("UserService: User not found for email: {}. Throw the UserNotFoundException at: {}", email, DateTimeConverter.formatInstantNow());
                    return new UserNotFoundException(ErrorsResponses.USER_NOT_FOUD.getMessage());
                });
    }

    /**
     * Converts a UserRequest object into a User entity.
     * This method performs the following steps:
     * - Logs the start of the conversion process.
     * - Maps the fields from the UserRequest object to a new User entity.
     * - The password is not stored in the User: it lives in PasswordParameters, per application.
     * - Sets default values for role and type of user.
     * - If an application code is provided, retrieves the corresponding application
     *   and associates it with the User entity.
     *
     * @author HahnGuil
     * @param request the UserRequest object containing user details
     * @return the User entity created from the UserRequest object
     */
    public User convertToEntity(UserRequest request){
        log.info("UserService: Convert user with email: {} to entity at: {}", request.getEmail(), DateTimeConverter.formatInstantNow());
        var user = new User();

        user.setUsername(request.getUsername());
        user.setPassword("");
        user.setBlockUser(false);
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPictureUrl(request.getPictureUrl());
        user.setRole(UserRole.USER_NORMAL);
        user.setUserApplicationRole(UserApplicationRole.USER);
        user.setTypeUser(TypeUser.valueOf(request.getTypeUser().toString()));

        if (request.getApplicationCode() != null) {
            var application = applicationService.findById(request.getApplicationCode());
            user.setApplications(Set.of(application));
        }
        return user;
    }

    /**
     * Updates the password of a user for one application only, resets its creation date
     * and removes the block of that application.
     *
     * @author HahnGuil
     * @param passwordParameters the password parameters (user + application) to update
     * @param newPassword the new encoded password
     * @param passwordCreateDate the date and time when the new password was created
     */
    @Transactional
    public void updatePassword(PasswordParameters passwordParameters, String newPassword, LocalDateTime passwordCreateDate) {
        log.info("UserService: update password for the user: {} on application: {} at: {}", passwordParameters.getUser().getUserId(), passwordParameters.getApplication().getId(), DateTimeConverter.formatInstantNow());
        passwordParameters.setPassword(newPassword);
        passwordParameters.setPasswordCreateDate(passwordCreateDate);
        passwordParameters.setBlockUser(false);
        passwordParametersRepository.save(passwordParameters);
    }

    /**
     * Creates a new user from an OAuth2User object.
     * This method performs the following steps:
     * - Logs the start of the user creation process.
     * - Converts the OAuth2User object into a UserRequest object.
     * - Converts the UserRequest object into a User entity.
     * - Saves the new User entity to the repository.
     * - Returns the newly created User entity.
     *
     * @author HahnGuil
     * @param oAuth2User the OAuth2User object containing user details from the OAuth provider
     * @return the newly created User entity
     */
    public User createNewUserFromOAuth (OAuth2User oAuth2User){
        log.info("UserService: Creating a new user: {}, from OAuth, at: {}", oAuth2User.getAttribute("email"), DateTimeConverter.formatInstantNow());
        var userRequest = convertToUserRequest(oAuth2User);
        var newUserFromOAuth = convertToEntity(userRequest);
        userRepository.save(newUserFromOAuth);
        return newUserFromOAuth;
    }

    /**
     * Retrieves the passwords (user + application) that will expire in exactly the given number of days,
     * according to the password policy of each application. Applications without a policy are ignored.
     *
     * @author HahnGuil
     * @param daysUntilExpiration number of days left until the password expires
     * @return the password parameters to be notified
     */
    public List<PasswordParameters> getUsersWithPasswordExpiringInDays(int daysUntilExpiration) {
        log.info("UserService: get users with password expiring in days: {}, at: {}", daysUntilExpiration, DateTimeConverter.formatInstantNow());
        var today = LocalDate.now(ZoneId.systemDefault());
        return findPasswordsWithExpiringPolicy().entrySet().stream()
                .filter(e -> {
                    var expirationDate = e.getValue().calculateExpirationDate(e.getKey().getPasswordCreateDate());
                    return expirationDate != null && ChronoUnit.DAYS.between(today, expirationDate.toLocalDate()) == daysUntilExpiration;
                })
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Blocks the passwords (user + application) already expired according to the policy of the application.
     * Applications without a policy are never blocked.
     *
     * @author HahnGuil
     */
    @Transactional
    public void findUserToBlock() {
        log.info("UserService: Find users to block at: {}", DateTimeConverter.formatInstantNow());
        var now = LocalDateTime.now(ZoneId.systemDefault());
        var expired = findPasswordsWithExpiringPolicy().entrySet().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getKey().getBlockUser()))
                .filter(e -> {
                    var expirationDate = e.getValue().calculateExpirationDate(e.getKey().getPasswordCreateDate());
                    return expirationDate != null && expirationDate.isBefore(now);
                })
                .map(Map.Entry::getKey)
                .toList();
        blockPasswords(expired);
    }

    private Map<PasswordParameters, PasswordPolicy> findPasswordsWithExpiringPolicy() {
        var policies = passwordPolicyRepository.findAllWithExpirationEnabled().stream()
                .collect(Collectors.toMap(p -> p.getApplication().getId(), Function.identity()));
        if (policies.isEmpty()) {
            return Map.of();
        }
        var result = new LinkedHashMap<PasswordParameters, PasswordPolicy>();
        passwordParametersRepository.findByApplicationIds(policies.keySet())
                .forEach(p -> result.put(p, policies.get(p.getApplication().getId())));
        return result;
    }

    /**
     * Validates that the JWT belongs to an active login of an admin user.
     *
     * @author HahnGuil
     * @param jwt the JWT of the request
     * @throws ForbiddenOperationException if the user is not an admin
     */
    public void validateAdminUser(Jwt jwt){
        var scope = jwt.getClaimAsString("scope");
        var tokenLogId = jwt.getClaimAsString("token_log_id");
        var isUserToken = ScopeToken.LOGIN_TOKEN.getValue().equals(scope) || ScopeToken.REGISTER_TOKEN.getValue().equals(scope);
        if(!isUserToken || tokenLogId == null || !tokenLogService.isTokenValid(UUID.fromString(tokenLogId))){
            throw new ForbiddenOperationException(ErrorsResponses.ONLY_ADMIN_CAN_CREATE_APPLICATION.getMessage());
        }

        var user = findByEmail(getUserEmailFromToken(jwt));
        if(user.getRole() != UserRole.USER_ADMIN && user.getRole() != UserRole.SUPER_USER){
            log.error("UserService: User: {} is not admin. Throw ForbiddenOperationException at: {}", user.getUserId(), DateTimeConverter.formatInstantNow());
            throw new ForbiddenOperationException(ErrorsResponses.ONLY_ADMIN_CAN_CREATE_APPLICATION.getMessage());
        }
    }

    /**
     * Associates an application with an OAuth user based on JWT claims.
     * This method performs the following steps:
     * - Logs the start of the association process.
     * - Reads the user type from the JWT and returns a message if the user is not OAuth.
     * - Retrieves the user ID from the JWT claim and loads both user and application entities.
     * - Checks whether the application is already associated with the user.
     * - If not associated yet, adds the application to the user and persists the change.
     *
     * @author HahnGuil
     * @param jwt JWT token containing user claims (e.g., userId and type_user)
     * @param applicationPublicId the public identifier of the application to be associated
     * @return SuccessResponse with the operation result message
     */
    @Transactional
    public SuccessResponse setApplicationToUser(Jwt jwt, UUID applicationPublicId){
        log.info("UserService: Start registration user to application at: {}", DateTimeConverter.formatInstantNow());
        SuccessResponse response = new SuccessResponse();
        String typeUser = jwt.getClaim("type_user");

        if(!isUserOauth(typeUser)){
            log.debug("UserService: Direct user does not need OAuth application registration");
            response.setMessage("User is not OAuth, is not necessary registered");
            return response;
        }

        var userIdToken = jwt.getClaim("user_id");
        var userId = UUID.fromString((String) userIdToken);
        var user = findById(userId);
        var application = applicationService.findByPublicId(applicationPublicId);

        if(isApplicationAlreadySetForUser(user, application)){
            log.info("UserService: Application not yet registered to the user: {}. Adding the application to the user {}", user.getUserId(), application);
            user.getApplications().add(application);
            userRepository.save(user);
        }

        log.info("UserService: End registration user to application at: {}", DateTimeConverter.formatInstantNow());
        response.setMessage("Application has been set for the user");
        return response;
    }

    public void updateUserApplicationRole(Jwt jwt, UUID applicationPublicId){
        var userEmail = getUserEmailFromToken(jwt);
        var user = findByEmail(userEmail);

        if (isUserRegisteredOnApplication(user, applicationPublicId)) {
            user.setUserApplicationRole(UserApplicationRole.ADMIN);
            userRepository.save(user);
            userDataService.updateUserByEmailHeader(jwt, userEmail);
        }
    }

    public UserProfileResponse getAuthenticatedUserProfile(Jwt jwt) {
        var userEmail = getUserEmailFromToken(jwt);
        var user = findByEmail(userEmail);

        var response = new UserProfileResponse();
        response.setUserName(user.getUsername());
        response.setFullName(buildFullName(user));
        response.setEmail(user.getEmail());
        return response;
    }

    @Transactional
    public void updateAuthenticatedUsername(Jwt jwt, UpdateUsernameRequest request) {
        var userEmail = getUserEmailFromToken(jwt);
        var user = findByEmail(userEmail);
        var requestedUserName = request.getUserName() == null ? null : request.getUserName().trim();

        if (requestedUserName.equals(user.getUsername())) {
            return;
        }

        if (userRepository.existsByUsernameIgnoreCaseAndUserIdNot(requestedUserName, user.getUserId())) {
            throw new ResourceAlreadyExistException(ErrorsResponses.USERNAME_ALREADY_REGISTER_ERROR.getMessage());
        }

        user.setUsername(requestedUserName);
        userRepository.save(user);
        userDataService.updateUserByEmailHeader(jwt, userEmail);
    }

    private boolean isUserRegisteredOnApplication(User user, UUID applicationPublicId) {
        return user.getApplications() != null &&
                user.getApplications().stream()
                        .filter(Objects::nonNull)
                        .map(Application::getPublicId)
                        .anyMatch(applicationPublicId::equals);
    }

    private String getUserEmailFromToken(Jwt jwt){
        return jwt.getSubject();
    }

    private boolean isUserOauth(String typeUser){
        return typeUser.equals(TypeUser.OAUTH_USER.toString());
    }



    private String buildFullName(User user) {
        var firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        var lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        return (firstName + " " + lastName).trim();
    }

    /**
     * Converts an OAuth2User object into a UserRequest object.
     * This method performs the following steps:
     * - Creates a new UserRequest object.
     * - Maps attributes from the OAuth2User object to the corresponding fields in the UserRequest object.
     * - Sets the application code to null and the user type to OAUTH_USER.
     *
     * @author HahnGuil
     * @param oAuth2User the OAuth2User object containing user details from the OAuth provider
     * @return the UserRequest object populated with the mapped user details
     */
    private UserRequest convertToUserRequest(OAuth2User oAuth2User){
        log.info("UserService: Converting OAuth user: {} to UserRequest at: {}", oAuth2User.getAttribute("email"), DateTimeConverter.formatInstantNow());
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(oAuth2User.getAttribute("name"));
        userRequest.setEmail(oAuth2User.getAttribute("email"));
        userRequest.setFirstName(oAuth2User.getAttribute("given_name"));
        userRequest.setLastName(oAuth2User.getAttribute("family_name"));
        userRequest.pictureUrl(oAuth2User.getAttribute("picture"));
        userRequest.setApplicationCode(null);
        userRequest.setTypeUser(UserRequest.TypeUserEnum.OAUTH_USER);
        return userRequest;
    }

    /**
     * Converts a User entity into a UserResponse object.
     * This method performs the following steps:
     * - Creates a new UserResponse object.
     * - Sets the user ID, full name, and email from the User entity.
     * - Generates a token for the user and saves the token log.
     * - Sets the generated token in the UserResponse object.
     *
     * @author HahnGuil
     * @param user the User entity containing user details
     * @return the UserResponse object populated with user details and a generated token
     */
    private UserResponse convertToUserResponse(User user, Application application){
        log.info("UserService: convert user: {}, to UserResponse, at: {}", user.getUserId(), DateTimeConverter.formatInstantNow());
        UserResponse userResponse = new UserResponse();
        userResponse.setUserId(user.getUserId());
        userResponse.setUserName(user.getFirstName() + " " + user.getLastName());
        userResponse.setEmail(user.getEmail());
        var token = tokenService.generateUserToken(user, generateTokenLog(user, application, ScopeToken.REGISTER_TOKEN), application);
        userResponse.setToken(token);
        var refreshToken = tokenService.generateRefreshToken(user, generateTokenLog(user, application, ScopeToken.REFRESH_TOKEN), application);
        userResponse.setRefreshToken(refreshToken);
        log.info("UserService: User: {}, converted to userResponse id: {}, at: {}", user.getUserId(), userResponse.getUserId(), DateTimeConverter.formatInstantNow());
        return userResponse;
    }

    /**
     * Checks if an application is already associated with a user.
     * This method performs the following steps:
     * - Logs the validation process to check if the application is registered for the user.
     * - Verifies if the user's list of applications contains the specified application.
     *
     * @author HahnGuil
     * @param user the User entity to check for the application association
     * @param application the Application entity to validate
     * @return true if the application is not associated with the user, false otherwise
     */
    private boolean isApplicationAlreadySetForUser(User user, Application application){
        log.info("UserService: Validate if user: {}, already register for this application {} at: {}", user.getUserId(), application, DateTimeConverter.formatInstantNow());
        return !user.getApplications().contains(application);
    }

    /**
     * Retrieves a user by their unique identifier.
     * This method performs the following steps:
     * - Queries the repository to find the user by their ID.
     * - If the user is not found, logs an error and throws a UserNotFoundException.
     *
     * @author HahnGuil
     * @param userId the unique identifier of the user to retrieve
     * @return the User entity associated with the provided ID
     * @throws UserNotFoundException if no user is found for the given ID
     */
    private User findById(UUID userId){
        return userRepository.findById(userId).orElseThrow(() -> {
            log.error("UserService: User not foud for this id: {}, throw UserNotFoundException at: {}", userId, DateTimeConverter.formatInstantNow());
            return new UserNotFoundException(ErrorsResponses.USER_NOT_FOUD.getMessage());
        });
    }

    /**
     * Blocks the passwords (user/application pair) with the USER_NORMAL role.
     * The block is independent per application.
     *
     * @author HahnGuil
     * @param passwordsToBlock the password parameters to be blocked
     */
    private void blockPasswords(List<PasswordParameters> passwordsToBlock) {
        log.info("UserService: Starting block of passwords at: {}", DateTimeConverter.formatInstantNow());
        passwordsToBlock.stream().filter(p -> p.getUser().getRole() == UserRole.USER_NORMAL).forEach(p -> {
            p.setBlockUser(true);
            passwordParametersRepository.save(p);
        });
        log.info("UserService: Finish block of passwords at: {}", DateTimeConverter.formatInstantNow());
    }

    /**
     * Generates a TokenLog for a given user and scope token.
     * This method performs the following steps:
     * - Calls the TokenLogService to save a new token log entry.
     * - Uses the current date and time as the timestamp for the token log.
     *
     * @author HahnGuil
     * @param user the User entity for whom the token log is being generated
     * @param application the application the session belongs to
     * @param scopeToken the ScopeToken enum representing the scope of the token
     * @return the generated TokenLog entity
     */
    private TokenLog generateTokenLog(User user, Application application, ScopeToken scopeToken){
        return tokenLogService.saveTokenLog(user, application, scopeToken, LocalDateTime.now(ZoneId.systemDefault()));
    }

}
