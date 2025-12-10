package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.UserEmailAlreadyExistException;
import br.com.hahn.auth.application.execption.UserNotFoundException;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeUser;
import br.com.hahn.auth.domain.enums.UserRole;
import br.com.hahn.auth.domain.model.Application;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.model.UserRequest;
import br.com.hahn.auth.domain.model.UserResponse;
import br.com.hahn.auth.domain.respository.UserRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final ApplicationService applicationService;
    private final TokenLogService tokenLogService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;


    /**
     * Creates a new user based on the provided user request.
     * This method performs the following steps:
     * - Logs the start of the user creation process.
     * - Checks if the email provided in the user request already exists.
     * - Converts the user request into a User entity and encodes the password.
     * - Saves the new user entity to the repository.
     * - Converts the saved user entity into a UserResponse object and returns it.
     *
     * @author HahnGuil
     * @param userRequest the request object containing user details
     * @return UserResponse containing the details of the created user
     * @throws UserEmailAlreadyExistException if the email is already registered
     */
    public UserResponse createUser(UserRequest userRequest){
        log.info("UserService: Starting user creation for user email: {} at: {}", userRequest.getEmail(), Instant.now());

        if(this.existsByEmail(userRequest.getEmail())){
            log.error("UserService: Email already registered for this user with email: {}. throw UserEmailAlreadyExistsException at: {}", userRequest.getEmail(), Instant.now());
            throw new UserEmailAlreadyExistException(ErrorsResponses.EMAIL_ALREADY_REGISTER_ERROR.getMessage());
        }

        log.info("UserService: Starting convert user with email: {} to entity at: {}", userRequest.getEmail(), Instant.now());
        var user = convertToEntity(userRequest, passwordEncoder.encode(userRequest.getPassword()));
        userRepository.save(user);
        return convertToUserResponse(user);
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
        log.info("UserService: Checking if the requested email exists. For email {} at {}", email,  Instant.now());
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
        log.info("UserService: Searching for user for email: {} at: {}", email, Instant.now());
        return userRepository.findByEmailWithApplications(email)
                .orElseThrow(() -> {
                    log.error("UserService: User not found for email: {}. Throw the UserNotFoundException at: {}", email, Instant.now());
                    return new UserNotFoundException(ErrorsResponses.USER_NOT_FOUD.getMessage());
                });
    }

    /**
     * Converts a UserRequest object into a User entity.
     * This method performs the following steps:
     * - Logs the start of the conversion process.
     * - Maps the fields from the UserRequest object to a new User entity.
     * - Encodes the password and sets it in the User entity.
     * - Sets default values for role and type of user.
     * - If an application code is provided, retrieves the corresponding application
     *   and associates it with the User entity.
     *
     * @author HahnGuil
     * @param request the UserRequest object containing user details
     * @param encodePassword the encoded password to be set in the User entity
     * @return the User entity created from the UserRequest object
     */
    public User convertToEntity(UserRequest request, String encodePassword){
        log.info("UserService: Convert user with email: {} to entity at: {}", request.getEmail(), Instant.now());
        var user = new User();

        user.setUsername(request.getUsername());
        user.setPassword(encodePassword);
        user.setPasswordCreateDate(LocalDateTime.now());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPictureUrl(request.getPictureUrl());
        user.setRole(UserRole.USER_NORMAL);
        user.setTypeUser(TypeUser.valueOf(request.getTypeUser().toString()));

        if (request.getApplicationCode() != null) {
            var application = applicationService.findById(request.getApplicationCode());
            user.setApplications(Set.of(application));
        }
        return user;
    }

    /**
     * Updates the password for a user identified by their email and ID.
     * This method performs the following steps:
     * - Logs the start of the password update process.
     * - Updates the user's password in the repository using the provided email, ID,
     *   new password, and password creation date.
     *
     * @author HahnGuil
     * @param email the email address of the user whose password is being updated
     * @param id the unique identifier of the user
     * @param newPassword the new password to set for the user
     * @param passwordCreateDate the date and time when the new password was created
     */
    @Transactional
    public void updatePassword(String email, UUID id, String newPassword, LocalDateTime passwordCreateDate) {
        log.info("UserService: update password for the user: {} at: {}", id, Instant.now());
        userRepository.updatePasswordByEmailAndId(newPassword, email, id, passwordCreateDate);
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
        log.info("UserService: Creating a new user: {}, from OAuth, at: {}", oAuth2User.getAttribute("email"), Instant.now());
        var userRequest = convertToUserRequest(oAuth2User);
        var newUserFromOAuth = convertToEntity(userRequest, "");
        userRepository.save(newUserFromOAuth);
        return newUserFromOAuth;
    }

    /**
     * Retrieves a list of users whose passwords are expiring within a specified number of days.
     * This method performs the following steps:
     * - Logs the start of the retrieval process.
     * - Calculates the date threshold based on the provided number of days until expiration.
     * - Queries the repository for users with passwords older than the calculated threshold.
     *
     * @author HahnGuil
     * @param daysUntilBlock the number of days until the passwords are considered expiring
     * @return a list of users whose passwords are expiring within the specified timeframe
     */
    public List<User> getUsersWithPasswordExpiringInDays(int daysUntilBlock) {
        log.info("UserService: get users with password expiring in dats: {}, at: {}", daysUntilBlock, Instant.now());
        var dateThreshold = LocalDateTime.now().minusDays(90L - daysUntilBlock);
        return userRepository.findUsersWithPasswordOlderThan(dateThreshold);
    }

    /**
     * Finds and blocks users whose passwords were updated within the last 90 days.
     * This method performs the following steps:
     * - Logs the start of the search for users to block.
     * - Calculates a reference date equal to 90 days ago.
     * - Retrieves users with passwords updated after the reference date and delegates
     *   the blocking logic to {@code blockUsers}.
     *
     * @author HahnGuil
     */
    public void findUserToBlock() {
        log.info("UserService: Find users to block at: {}", Instant.now());
        var referenceData = LocalDateTime.now().minusDays(90);
        blockUsers(userRepository.findUsersWithPasswordNewerThan(referenceData));
    }

    /**
     * Associates an application with a user based on their IDs.
     * This method performs the following steps:
     * - Logs the start of the process to associate the application with the user.
     * - Retrieves the user and application entities using their respective IDs.
     * - Checks if the application is already associated with the user.
     * - If not, adds the application to the user's list of applications and saves the user.
     *
     * @author HahnGuil
     * @param userId the unique identifier of the user
     * @param applicationId the unique identifier of the application
     */
    @Transactional
    public void setApplicationToUser(UUID userId, Long applicationId){
        log.info("UserService: Set Application: {}, to User: {}, at: {}", applicationId, userId, Instant.now());
        var user = findById(userId);
        var application = applicationService.findById(applicationId);

        if(isApplicationAlreadySetForUser(user, application)){
            log.info("UserService: Application not yet registered to the user: {}. Adding the application to the user {}", user.getUserId(), application);
            user.getApplications().add(application);
            userRepository.save(user);
        }
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
        log.info("UserService: Converting OAuth user: {} to UserRequest at: {}", oAuth2User.getAttribute("email"), Instant.now());
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
    private UserResponse convertToUserResponse(User user){
        log.info("UserService: convert user: {}, to UserResponse, at: {}", user.getUserId(), Instant.now());
        UserResponse userResponse = new UserResponse();
        userResponse.setUserId(user.getUserId());
        userResponse.setUserName(user.getFirstName() + " " + user.getLastName());
        userResponse.setEmail(user.getEmail());
        var token = tokenService.generateToken(user, tokenLogService.saveTokenLog(user, ScopeToken.REGISTER_TOKEN, LocalDateTime.now()));
        userResponse.setToken(token);
        log.info("UserService: User: {}, converted to userResponse id: {}, at: {}", user.getUserId(), userResponse.getUserId(), Instant.now());
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
        log.info("UserService: Validate if user: {}, already register for this application {} at: {}", user.getUserId(), application, Instant.now());
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
            log.error("UserService: User not foud for this id: {}, throw UserNotFoundException at: {}", userId, Instant.now());
            return new UserNotFoundException(ErrorsResponses.USER_NOT_FOUD.getMessage());
        });
    }


    /**
     * Blocks a list of users by setting their block status to true.
     * This method performs the following steps:
     * - Logs the start of the blocking process.
     * - Checks if the provided list of users is null or empty and logs a message if so.
     * - Filters the list to include only users with the role USER_NORMAL.
     * - Sets the block status to true for each filtered user and saves the changes to the repository.
     * - Logs the completion of the blocking process.
     *
     * @author HahnGuil
     * @param usersToBlock the list of users to be blocked
     */
    private void blockUsers(List<User> usersToBlock) {
        log.info("UserService: Starting block user of the list at: {}", Instant.now());
        if (usersToBlock == null || usersToBlock.isEmpty()) {
            log.info("UserService: No users to block at: {}", Instant.now());
            return;
        }
        usersToBlock.stream().filter(u -> u.getRole() == UserRole.USER_NORMAL).forEach(u -> {
                    u.setBlockUser(true);
                    userRepository.save(u);
                });
        log.info("UserService: Finish block user from list at: {}", Instant.now());
    }

}
