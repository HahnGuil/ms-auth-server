package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.InvalidCredentialsException;
import br.com.hahn.auth.application.execption.InvalidTokenException;
import br.com.hahn.auth.application.execption.UserBlockException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.model.LoginRequest;
import br.com.hahn.auth.domain.model.LoginResponse;
import br.com.hahn.auth.domain.model.TokenLog;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.infrastructure.security.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private TokenLogService tokenLogService;

    @Mock
    private TokenService tokenService;

    @Mock
    private LoggedNowService loggedNowService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginUserSuccessfullyWhenCredentialsAreValid() {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password");
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");

        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(user);
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(tokenLogService.saveTokenLog(any(), eq(ScopeToken.LOGIN_TOKEN), any())).thenReturn(new TokenLog());
        when(tokenService.generateToken(any(), any())).thenReturn("accessToken");
        when(tokenLogService.saveTokenLog(any(), eq(ScopeToken.REFRESH_TOKEN), any())).thenReturn(new TokenLog());
        when(tokenService.generateRefreshToken(any(), any())).thenReturn("refreshToken");

        LoginResponse response = authService.userLogin(loginRequest);

        assertNotNull(response);
        assertEquals("user@example.com", response.getEmail());
        assertEquals("accessToken", response.getToken());
        assertEquals("refreshToken", response.getRefreshToken());
    }

    @Test
    void shouldThrowInvalidCredentialsExceptionWhenPasswordDoesNotMatch() {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "wrongPassword");
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");

        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(user);
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.userLogin(loginRequest));
    }

    @Test
    void shouldThrowUserBlockExceptionWhenUserIsBlocked() {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password");
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");
        user.setBlockUser(true);

        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(user);

        assertThrows(UserBlockException.class, () -> authService.userLogin(loginRequest));
    }

    @Test
    void shouldLogOffUserSuccessfully() {
        Jwt jwt = mock(Jwt.class);
        UUID userId = UUID.randomUUID();

        when(jwt.getClaim("user_id")).thenReturn(userId.toString());

        authService.logOffUser(jwt);

        verify(loggedNowService).deleteByUserId(userId);
        verify(tokenLogService).deactivateActiveToken(userId, TypeInvalidation.LOG_OFF);
    }

    @Test
    void shouldThrowInvalidTokenExceptionWhenTokenIsNotRefreshToken() throws Exception {
        UUID tokenLogId = UUID.randomUUID();
        TokenLog tokenLog = new TokenLog();
        tokenLog.setScopeToken(ScopeToken.LOGIN_TOKEN);

        when(tokenLogService.findById(tokenLogId)).thenReturn(tokenLog);

        Method method = AuthService.class.getDeclaredMethod("isRefreshToken", UUID.class);
        method.setAccessible(true);

        assertThrows(InvocationTargetException.class, () -> method.invoke(authService, tokenLogId));

        try {
            method.invoke(authService, tokenLogId);
            fail("Expected InvalidTokenException to be thrown");
        } catch (InvocationTargetException e) {
            assertInstanceOf(InvalidTokenException.class, e.getCause());
        }
    }
}