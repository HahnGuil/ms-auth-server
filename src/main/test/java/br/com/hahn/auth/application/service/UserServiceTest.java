package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.UserEmailAlreadyExistException;
import br.com.hahn.auth.application.execption.UserNotFoundException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.model.TokenLog;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.model.UserRequest;
import br.com.hahn.auth.domain.model.UserResponse;
import br.com.hahn.auth.domain.respository.UserRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenLogService tokenLogService;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("Should create a new user successfully when email does not exist")
        void createUserSuccessfully() {
            UserRequest userRequest = new UserRequest();
            userRequest.setEmail("test@example.com");
            userRequest.setPassword("password123");
            userRequest.setUsername("testuser");
            userRequest.setFirstName("Test");
            userRequest.setLastName("User");
            userRequest.setTypeUser(UserRequest.TypeUserEnum.DIRECT_USER);

            User savedUser = new User();
            savedUser.setUserId(UUID.randomUUID());
            savedUser.setEmail(userRequest.getEmail());
            savedUser.setFirstName(userRequest.getFirstName());
            savedUser.setLastName(userRequest.getLastName());

            TokenLog registerTokenLog = new TokenLog();
            registerTokenLog.setIdTokenLog(UUID.randomUUID());

            TokenLog refreshTokenLog = new TokenLog();
            refreshTokenLog.setIdTokenLog(UUID.randomUUID());

            when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(userRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(tokenLogService.saveTokenLog(any(User.class), eq(ScopeToken.REGISTER_TOKEN), any())).thenReturn(registerTokenLog);
            when(tokenLogService.saveTokenLog(any(User.class), eq(ScopeToken.REFRESH_TOKEN), any())).thenReturn(refreshTokenLog);
            when(tokenService.generateToken(any(User.class), any(TokenLog.class))).thenReturn("test-token");
            when(tokenService.generateRefreshToken(any(User.class), any(TokenLog.class))).thenReturn("test-refresh-token");

            UserResponse response = userService.createUser(userRequest);

            assertNotNull(response);
            assertEquals(userRequest.getEmail(), response.getEmail());
            verify(userRepository).existsByEmail(userRequest.getEmail());
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode(userRequest.getPassword());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void createUserThrowsExceptionWhenEmailExists() {
            UserRequest userRequest = new UserRequest();
            userRequest.setEmail("test@example.com");

            when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

            assertThrows(UserEmailAlreadyExistException.class, () -> userService.createUser(userRequest));
            verify(userRepository).existsByEmail(userRequest.getEmail());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("Should return user when email exists")
        void findByEmailReturnsUser() {
            String email = "test@example.com";
            User user = new User();
            user.setEmail(email);

            when(userRepository.findByEmailWithApplications(email)).thenReturn(Optional.of(user));

            User result = userService.findByEmail(email);

            assertNotNull(result);
            assertEquals(email, result.getEmail());
            verify(userRepository).findByEmailWithApplications(email);
        }

        @Test
        @DisplayName("Should throw exception when email does not exist")
        void findByEmailThrowsExceptionWhenEmailNotFound() {
            String email = "test@example.com";

            when(userRepository.findByEmailWithApplications(email)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> userService.findByEmail(email));
            verify(userRepository).findByEmailWithApplications(email);
        }
    }
}
