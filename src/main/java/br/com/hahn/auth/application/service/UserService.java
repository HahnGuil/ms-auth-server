package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.request.UserRequestDTO;
import br.com.hahn.auth.application.execption.DataBaseServerException;
import br.com.hahn.auth.application.execption.UserNotFoundException;
import br.com.hahn.auth.domain.enums.TypeUser;
import br.com.hahn.auth.domain.enums.UserRole;
import br.com.hahn.auth.domain.model.Application;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ApplicationService applicationService;

    public UserService(UserRepository userRepository, ApplicationService applicationService) {
        this.userRepository = userRepository;
        this.applicationService = applicationService;
    }

    public boolean existsByEmail(String email){
        logger.info("UserService: Exist user by email");
        return userRepository.existsByEmail(email);
    }

    public User findByEmail(String email){
        logger.info("UserService: Find user by email");
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found. Check email and password or register a new user."));
    }

    public User convertToEntity(UserRequestDTO userRequestDTO, String encodePassword){
        logger.info("UserService: Convert to entity");
        User user = new User();

        user.setUsername(userRequestDTO.userName());
        user.setPassword(encodePassword);
        user.setPasswordCreateDate(LocalDateTime.now());
        user.setEmail(userRequestDTO.email());
        user.setFirstName(userRequestDTO.firstName());
        user.setLastName(userRequestDTO.lastName());
        user.setPictureUrl(userRequestDTO.pictureUrl());
        user.setRole(UserRole.valueOf(userRequestDTO.typeUser()));

        if (userRequestDTO.application() != null) {
            Application application = applicationService.findById(userRequestDTO.application());
            user.setApplications(Set.of(application));
        }
        return user;
    }

    public List<User> getUsersWithPasswordExpiringInDays(int daysUntilBlock) {
        logger.info("UserService: get users with password expiring in days");
        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(90L - daysUntilBlock);
        return userRepository.findUsersWithPasswordOlderThan(dateThreshold);
    }

    public void findUserToBlock() {
        logger.info("UserService: Find users to block");
        LocalDateTime referenceData = LocalDateTime.now().minusDays(90);
        blockUsers(userRepository.findUsersWithPasswordNewerThan(referenceData));
    }

    @Transactional
    public void saveUser(User user){
        logger.info("UserService: save user");
        try {
            userRepository.save(user);
        }catch (DataAccessException _){
            logger.info("UserService: Can not save on data base. Throw exception");
            throw new DataBaseServerException("Can't save user on database");
        }

    }

    @Transactional
    public void updatePassword(String email, UUID id, String newPassword, LocalDateTime passwordCreateDate) {
        logger.info("UserService: update user password");
        userRepository.updatePasswordByEmailAndId(newPassword, email, id, passwordCreateDate);
    }

    private void blockUsers(List<User> usersToBlock) {
        logger.info("UserService: block users");
        for (User user : usersToBlock) {
            user.setBlockUser(true);
            userRepository.save(user);
        }
    }

    public User findByIdWithApplications(String email) {
        logger.info("UserService: find by with application");
        return userRepository.findByEmailWithApplications(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for this application"));
    }

    protected UserRequestDTO convertOAuthUserToRequestDTO(OAuth2User oAuth2User){
        logger.info("UserService: convert OAuth to DTO");
        return new UserRequestDTO(
                oAuth2User.getAttribute("name"),
                oAuth2User.getAttribute("email"),
                "",
                oAuth2User.getAttribute("given_name"),
                oAuth2User.getAttribute("family_name"),
                oAuth2User.getAttribute("picture"),
                null,
                TypeUser.OAUTH_USER.toString()
        );
    }

}
