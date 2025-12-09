package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.DataBaseServerException;
import br.com.hahn.auth.application.execption.UserEmailAlreadyExistException;
import br.com.hahn.auth.application.execption.UserNotFoundException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeUser;
import br.com.hahn.auth.domain.enums.UserRole;
import br.com.hahn.auth.domain.model.Application;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.model.UserRequest;
import br.com.hahn.auth.domain.model.UserResponse;
import br.com.hahn.auth.domain.respository.UserRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
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
@AllArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final ApplicationService applicationService;
    private final AuthService authService;
    private final LoginLogService loginLogService;
    private final TokenService tokenService;

//    REFATORAÇÃO
    public UserResponse createUser(UserRequest userRequest){
        log.info("UserService: Starting user creation for user email: {} at: {}", userRequest.getEmail(), Instant.now());

        if(this.existsByEmail(userRequest.getEmail())){
            log.error("UserService: Email already registered for this user with email: {}, throw exception {}", userRequest.getEmail(), Instant.now());
            throw new UserEmailAlreadyExistException("Email already registered. Please log in or recover your password.");
        }

        log.info("UserService: Starting convert user with email: {} to entity at: {}", userRequest.getEmail(), Instant.now());
        var user = this.convertToEntity(userRequest, authService.encodePassword(userRequest.getPassword()));
        userRepository.save(user);
        return convertToUserResponse(user);

    }

    private UserResponse convertToUserResponse(User user){
        UserResponse userResponse = new UserResponse();
        userResponse.setUserId(user.getUserId());
        userResponse.setUserName(user.getFirstName() + " " + user.getLastName());
        userResponse.setEmail(user.getEmail());
        var token = tokenService.generateToken(user, loginLogService.saveLoginLog(user, ScopeToken.REGISTER_TOKEN, LocalDateTime.now()));
        userResponse.setToken(token);
        return userResponse;
    }

    public boolean existsByEmail(String email){
        log.info("UserService: Checking if the requested email exists. For email {} at {}", email,  Instant.now());
        return userRepository.existsByEmail(email);
    }

    public User findByEmail(String email){
        log.info("UserService: Searching for user for email: {} at: {}", email, Instant.now());
        return userRepository.findByEmailWithApplications(email)
                .orElseThrow(() -> {
                    log.error("UserService: User not found for email: {} at {}", email, Instant.now());
                    return new UserNotFoundException("User not found. Check email and password or register a new user.");
                });
    }

    public User convertToEntity(UserRequest request, String encodePassword){
        log.info("UserService: Convert user with email: {} to entity at: {}", request.getEmail(), Instant.now());
        User user = new User();

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

    // CÓDIGO ANTIGO -------------------

    public List<User> getUsersWithPasswordExpiringInDays(int daysUntilBlock) {
        log.info("UserService: get users with password expiring in days");
        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(90L - daysUntilBlock);
        return userRepository.findUsersWithPasswordOlderThan(dateThreshold);
    }

    public void findUserToBlock() {
        log.info("UserService: Find users to block");
        LocalDateTime referenceData = LocalDateTime.now().minusDays(90);
        blockUsers(userRepository.findUsersWithPasswordNewerThan(referenceData));
    }

    @Transactional
    public void saveUser(User user){
        log.info("UserService: save user");
        try {
            userRepository.save(user);
        }catch (DataAccessException _){
            log.error("UserService: Can not save on data base. Throw exception");
            throw new DataBaseServerException("Can't save user on database");
        }

    }

    @Transactional
    public void updatePassword(String email, UUID id, String newPassword, LocalDateTime passwordCreateDate) {
        log.info("UserService: update user password");
        userRepository.updatePasswordByEmailAndId(newPassword, email, id, passwordCreateDate);
    }

    public User findByIdWithApplications(String email) {
        log.info("UserService: find by with application");
        return userRepository.findByEmailWithApplications(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for this application"));
    }

    @Transactional
    public void setApplicationToUser(UUID userId, Long applicationId){
        log.info("UserService: Set Application to User");
        User user = findById(userId);
        Application application = applicationService.findById(applicationId);

        if(isApplicationAlreadySetForUser(user, application)){
            log.info("UserService: Application not yet registered to the user. Adding the application to the user {}", application);
            user.getApplications().add(application);
            userRepository.save(user);
        }
    }

    public User createNewUserFromOAuth (OAuth2User oAuth2User){
        log.info("UserService: Creating a new user from an OAuth user");
        var userRequest = convertToUserRequest(oAuth2User);
        return convertToEntity(userRequest, "");
    }

    private UserRequest convertToUserRequest(OAuth2User oAuth2User){
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

    private boolean isApplicationAlreadySetForUser(User user, Application application){
        log.info("UserService: Validade if user aleady register for this application {}", application);
        return !user.getApplications().contains(application);
    }

    private User findById(UUID userId){
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found for this id"));
    }

    private void blockUsers(List<User> usersToBlock) {
        log.info("UserService: block users");
        for (User user : usersToBlock) {
            if(user.getRole() == UserRole.USER_NORMAL){
                user.setBlockUser(true);
                userRepository.save(user);
            }
        }
        log.info("UserService: finish block users");
    }
}
