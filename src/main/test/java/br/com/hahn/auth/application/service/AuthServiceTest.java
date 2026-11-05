package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.InvalidCredentialsException;
import br.com.hahn.auth.application.execption.InvalidTokenException;
import br.com.hahn.auth.application.execption.UserBlockException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.TypeInvalidation;
import br.com.hahn.auth.domain.enums.TypeUser;
import br.com.hahn.auth.domain.model.*;
import br.com.hahn.auth.infrastructure.security.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserService userService;
    @Mock TokenLogService tokenLogService;
    @Mock TokenService tokenService;
    @Mock LoggedNowService loggedNowService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ApplicationService applicationService;
    @InjectMocks AuthService authService;

    private final UUID appId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void loginUsesApplicationSpecificPasswordAndSession() {
        var request = new LoginRequest(appId, "user@example.com", "Password!1");
        var user = new User(); user.setUserId(UUID.randomUUID()); user.setEmail(request.getEmail());
        user.setFirstName("First"); user.setLastName("Last"); user.setTypeUser(TypeUser.DIRECT_USER);
        var app = new Application(); app.setId(7L); app.setPublicId(appId); app.setNameApplication("app");
        var parameters = new PasswordParameters(); parameters.setUser(user); parameters.setApplication(app); parameters.setPassword("hash");
        var loginLog = new TokenLog(); var refreshLog = new TokenLog();
        when(userService.findByEmail(request.getEmail())).thenReturn(user);
        when(userService.findPasswordParameters(user, appId)).thenReturn(parameters);
        when(passwordEncoder.matches(request.getPassword(), "hash")).thenReturn(true);
        when(loggedNowService.findByUserIdAndApplication(user.getUserId(), appId)).thenReturn(List.of());
        when(tokenLogService.saveTokenLog(eq(user), eq(app), eq(ScopeToken.LOGIN_TOKEN), any())).thenReturn(loginLog);
        when(tokenLogService.saveTokenLog(eq(user), eq(app), eq(ScopeToken.REFRESH_TOKEN), any())).thenReturn(refreshLog);
        when(tokenService.generateUserToken(user, loginLog, app)).thenReturn("access");
        when(tokenService.generateRefreshToken(user, refreshLog, app)).thenReturn("refresh");

        LoginResponse response = authService.userLogin(request);

        assertEquals("user@example.com", response.getEmail());
        assertEquals("access", response.getToken());
        assertEquals("refresh", response.getRefreshToken());
        verify(userService).findPasswordParameters(user, appId);
        verify(loggedNowService).findByUserIdAndApplication(user.getUserId(), appId);
        verify(tokenLogService).saveTokenLog(eq(user), eq(app), eq(ScopeToken.LOGIN_TOKEN), any());
    }

    @Test
    void rejectsWrongPasswordForApplication() {
        var request = new LoginRequest(appId, "user@example.com", "wrong");
        var user = new User(); user.setUserId(UUID.randomUUID()); user.setTypeUser(TypeUser.DIRECT_USER);
        var app = new Application(); app.setPublicId(appId);
        var parameters = new PasswordParameters(); parameters.setApplication(app); parameters.setPassword("hash");
        when(userService.findByEmail(request.getEmail())).thenReturn(user);
        when(userService.findPasswordParameters(user, appId)).thenReturn(parameters);
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);
        assertThrows(InvalidCredentialsException.class, () -> authService.userLogin(request));
    }

    @Test
    void blocksOnlyApplicationPasswordWhenBlocked() {
        var request = new LoginRequest(appId, "user@example.com", "Password!1");
        var user = new User(); user.setUserId(UUID.randomUUID()); user.setTypeUser(TypeUser.DIRECT_USER);
        var parameters = new PasswordParameters(); parameters.setBlockUser(true);
        when(userService.findByEmail(request.getEmail())).thenReturn(user);
        when(userService.findPasswordParameters(user, appId)).thenReturn(parameters);
        assertThrows(UserBlockException.class, () -> authService.userLogin(request));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void logoffDeletesOnlyTheSessionForJwtApplication() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("user_id")).thenReturn(userId.toString());
        when(jwt.getClaimAsString("application_public_id")).thenReturn(appId.toString());
        authService.logOffUser(jwt);
        verify(loggedNowService).deleteByUserIdAndApplication(userId, appId);
        verify(tokenLogService).deactivateActiveUserToken(userId, appId, TypeInvalidation.LOG_OFF);
    }

    @Test
    void replacesExistingSessionOnlyForSameApplication() {
        var request = new LoginRequest(appId, "user@example.com", "pass");
        var user = new User(); user.setUserId(UUID.randomUUID()); user.setTypeUser(TypeUser.DIRECT_USER);
        var app = new Application(); app.setPublicId(appId);
        var params = new PasswordParameters(); params.setApplication(app); params.setPassword("hash");
        when(userService.findByEmail(request.getEmail())).thenReturn(user);
        when(userService.findPasswordParameters(user, appId)).thenReturn(params);
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);
        when(loggedNowService.findByUserIdAndApplication(user.getUserId(), appId)).thenReturn(List.of(new LoggedNow()));
        when(tokenLogService.saveTokenLog(eq(user), eq(app), any(), any())).thenReturn(new TokenLog());
        when(tokenService.generateUserToken(eq(user), any(), eq(app))).thenReturn("access");
        when(tokenService.generateRefreshToken(eq(user), any(), eq(app))).thenReturn("refresh");
        authService.userLogin(request);
        verify(loggedNowService).deleteByUserIdAndApplication(user.getUserId(), appId);
        verify(tokenLogService).deactivateActiveUserToken(user.getUserId(), appId, TypeInvalidation.NEW_LOGIN);
    }

    @Test
    void refreshesUserTokenForTheApplicationFromJwt() {
        UUID tokenId = UUID.randomUUID(), userId = UUID.randomUUID();
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("token_log_id")).thenReturn(tokenId.toString());
        when(jwt.getClaim("user_id")).thenReturn(userId.toString());
        when(jwt.getSubject()).thenReturn("user@example.com");
        when(jwt.getClaimAsString("application_public_id")).thenReturn(appId.toString());
        TokenLog refresh = new TokenLog(); refresh.setScopeToken(ScopeToken.REFRESH_TOKEN);
        when(tokenLogService.findById(tokenId)).thenReturn(refresh);
        when(tokenLogService.isTokenValid(tokenId)).thenReturn(true);
        User user = new User(); user.setUserId(userId); user.setEmail("user@example.com");
        Application app = new Application(); app.setPublicId(appId);
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(applicationService.findByPublicId(appId)).thenReturn(app);
        when(tokenLogService.saveTokenLog(eq(user), eq(app), any(), any())).thenReturn(new TokenLog());
        when(tokenService.generateUserToken(eq(user), any(), eq(app))).thenReturn("new-access");
        when(tokenService.generateRefreshToken(eq(user), any(), eq(app))).thenReturn("new-refresh");
        LoginResponse response = authService.generateNewTokenForUser(jwt);
        assertEquals("new-access", response.getToken());
        verify(tokenLogService).deactivateActiveUserToken(userId, appId, TypeInvalidation.USER_REFRESH);
    }

    @Test
    void rejectsExpiredUserRefreshToken() {
        UUID tokenId = UUID.randomUUID(); Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("token_log_id")).thenReturn(tokenId.toString());
        TokenLog log = new TokenLog(); log.setScopeToken(ScopeToken.REFRESH_TOKEN);
        when(tokenLogService.findById(tokenId)).thenReturn(log);
        when(tokenLogService.isTokenValid(tokenId)).thenReturn(false);
        assertThrows(InvalidCredentialsException.class, () -> authService.generateNewTokenForUser(jwt));
    }

    @Test
    void refreshesApplicationTokenAndRejectsMismatchedApplication() {
        UUID logId = UUID.randomUUID(); Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("token_log_id")).thenReturn(logId.toString());
        when(jwt.getSubject()).thenReturn(appId.toString());
        TokenLog log = new TokenLog(); log.setScopeToken(ScopeToken.APPLICATION_REFRESH_TOKEN);
        when(tokenLogService.findById(logId)).thenReturn(log);
        when(tokenLogService.isTokenValid(logId)).thenReturn(true);
        Application app = new Application(); app.setPublicId(appId); app.setNameApplication("app");
        when(applicationService.findByPublicId(appId)).thenReturn(app);
        when(tokenLogService.saveApplicationTokenLog(eq(app), any(), any())).thenReturn(new TokenLog());
        when(tokenService.generateApplicationToken(eq(app), any())).thenReturn("app-token");
        when(tokenService.generateApplicationRefreshToken(eq(app), any())).thenReturn("app-refresh");
        var response = authService.generateNewTokenForApplication(jwt, appId);
        assertEquals("app-token", response.getApplicationToken());
        verify(tokenLogService).deactiveActiveApplicationToken(appId, TypeInvalidation.APPLICATION_REFRESH);
        assertThrows(InvalidTokenException.class, () -> authService.generateNewTokenForApplication(jwt, UUID.randomUUID()));
    }

    @Test
    void oauthLoginCreatesUserOrUsesExistingAccount() {
        var oauth = mock(org.springframework.security.oauth2.core.user.OAuth2User.class);
        when(oauth.getAttribute("email")).thenReturn("oauth@example.com");
        when(userService.existsByEmail("oauth@example.com")).thenReturn(true);
        User existing = new User(); existing.setUserId(UUID.randomUUID()); existing.setEmail("oauth@example.com");
        when(userService.findByEmail("oauth@example.com")).thenReturn(existing);
        when(tokenLogService.saveTokenLog(eq(existing), isNull(), any(), any())).thenReturn(new TokenLog());
        when(tokenService.generateUserToken(eq(existing), any())).thenReturn("user-token");
        when(tokenService.generateRefreshToken(eq(existing), any(), isNull())).thenReturn("refresh-token");
        assertEquals("user-token", authService.processOAuthUser(oauth).getToken());
        verify(userService, never()).createNewUserFromOAuth(oauth);
    }

}
