package br.com.hahn.auth.application.service;

import java.time.ZoneId;
import br.com.hahn.auth.application.execption.ForbiddenOperationException;
import br.com.hahn.auth.application.execption.UserEmailAlreadyExistException;
import br.com.hahn.auth.application.execption.UserNotFoundException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.enums.UserRole;
import br.com.hahn.auth.domain.model.*;
import br.com.hahn.auth.domain.respository.PasswordParametersRepository;
import br.com.hahn.auth.domain.respository.PasswordPolicyRepository;
import br.com.hahn.auth.domain.respository.UserRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private static final UUID APP_PUBLIC_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    @Mock UserRepository userRepository;
    @Mock PasswordParametersRepository passwordParametersRepository;
    @Mock PasswordPolicyRepository passwordPolicyRepository;
    @Mock ApplicationService applicationService;
    @Mock TokenLogService tokenLogService;
    @Mock TokenService tokenService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserDataService userDataService;
    @InjectMocks UserService userService;

    private Application application() { Application app = new Application(); app.setId(5L); app.setPublicId(APP_PUBLIC_ID); return app; }
    private UserRequest request() {
        UserRequest request = new UserRequest(); request.setEmail("test@example.com"); request.setPassword("Password!1");
        request.setUsername("test"); request.setFirstName("First"); request.setLastName("Last");
        request.setTypeUser(UserRequest.TypeUserEnum.DIRECT_USER); request.setApplicationCode(5L); return request;
    }

    @Test
    void createsUserAndStoresPasswordForApplication() {
        var req = request(); var app = application();
        when(applicationService.findById(5L)).thenReturn(app);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encoded");
        when(userRepository.findByEmailWithApplications(req.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> { User saved = inv.getArgument(0); saved.setUserId(UUID.randomUUID()); return saved; });
        when(tokenLogService.saveTokenLog(any(), eq(app), any(), any())).thenReturn(new TokenLog());
        when(tokenService.generateUserToken(any(), any(), eq(app))).thenReturn("access");
        when(tokenService.generateRefreshToken(any(), any(), eq(app))).thenReturn("refresh");
        var response = userService.createUser(req);
        assertEquals(req.getEmail(), response.getEmail());
        assertEquals("access", response.getToken());
        verify(passwordParametersRepository).save(argThat(p -> "encoded".equals(p.getPassword()) && p.getApplication() == app && p.getUser().getEmail().equals(req.getEmail())));
    }

    @Test
    void permitsSameEmailInDifferentApplicationWithIndependentPassword() {
        var req = request(); var app = application();
        var existing = new User(); existing.setUserId(UUID.randomUUID()); existing.setEmail(req.getEmail()); existing.setApplications(Set.of());
        when(applicationService.findById(5L)).thenReturn(app);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("app-specific-hash");
        when(userRepository.findByEmailWithApplications(req.getEmail())).thenReturn(Optional.of(existing));
        when(passwordParametersRepository.existsByUserUserIdAndApplicationId(existing.getUserId(), app.getId())).thenReturn(false);
        when(tokenLogService.saveTokenLog(any(), eq(app), any(), any())).thenReturn(new TokenLog());
        when(tokenService.generateUserToken(any(), any(), eq(app))).thenReturn("access");
        when(tokenService.generateRefreshToken(any(), any(), eq(app))).thenReturn("refresh");
        userService.createUser(req);
        verify(passwordParametersRepository).save(argThat(p -> "app-specific-hash".equals(p.getPassword())));
    }

    @Test
    void rejectsEmailAlreadyRegisteredInSameApplication() {
        var req = request(); var app = application();
        var existing = new User(); existing.setUserId(UUID.randomUUID()); existing.setEmail(req.getEmail());
        when(applicationService.findById(5L)).thenReturn(app);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encoded");
        when(userRepository.findByEmailWithApplications(req.getEmail())).thenReturn(Optional.of(existing));
        when(passwordParametersRepository.existsByUserUserIdAndApplicationId(existing.getUserId(), app.getId())).thenReturn(true);
        assertThrows(UserEmailAlreadyExistException.class, () -> userService.createUser(req));
        verify(passwordParametersRepository, never()).save(any());
    }

    @Test
    void findsPasswordParametersByUserAndApplication() {
        User user = new User(); user.setUserId(UUID.randomUUID());
        var parameters = new PasswordParameters();
        when(passwordParametersRepository.findByUserAndApplicationPublicId(user.getUserId(), APP_PUBLIC_ID)).thenReturn(Optional.of(parameters));
        assertSame(parameters, userService.findPasswordParameters(user, APP_PUBLIC_ID));
        when(passwordParametersRepository.findByUserAndApplicationPublicId(user.getUserId(), APP_PUBLIC_ID)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findPasswordParameters(user, APP_PUBLIC_ID));
    }

    @Test
    void updatesOnlyTheSuppliedPasswordParametersAndUnblocksIt() {
        var params = new PasswordParameters(); params.setBlockUser(true); params.setUser(new User()); params.setApplication(application());
        LocalDateTime changedAt = LocalDateTime.now(ZoneId.systemDefault());
        userService.updatePassword(params, "new-hash", changedAt);
        assertEquals("new-hash", params.getPassword()); assertEquals(changedAt, params.getPasswordCreateDate()); assertFalse(params.getBlockUser());
        verify(passwordParametersRepository).save(params);
    }

    @Test
    void validatesOnlyActiveAdminLoginTokens() {
        Jwt jwt = mock(Jwt.class); UUID tokenId = UUID.randomUUID();
        when(jwt.getClaimAsString("scope")).thenReturn(ScopeToken.LOGIN_TOKEN.getValue());
        when(jwt.getClaimAsString("token_log_id")).thenReturn(tokenId.toString());
        when(jwt.getSubject()).thenReturn("admin@example.com");
        when(tokenLogService.isTokenValid(tokenId)).thenReturn(true);
        User admin = new User(); admin.setRole(UserRole.USER_ADMIN);
        when(userRepository.findByEmailWithApplications("admin@example.com")).thenReturn(Optional.of(admin));
        assertDoesNotThrow(() -> userService.validateAdminUser(jwt));
        verify(tokenLogService).isTokenValid(tokenId);
    }

    @Test
    void forbidsNonAdminOrInactiveToken() {
        Jwt jwt = mock(Jwt.class); UUID tokenId = UUID.randomUUID();
        when(jwt.getClaimAsString("scope")).thenReturn(ScopeToken.LOGIN_TOKEN.getValue());
        when(jwt.getClaimAsString("token_log_id")).thenReturn(tokenId.toString());
        when(tokenLogService.isTokenValid(tokenId)).thenReturn(false);
        assertThrows(ForbiddenOperationException.class, () -> userService.validateAdminUser(jwt));
    }

    @Test
    void warnsAndBlocksAccordingToEnabledApplicationPolicy() {
        var app = application(); var params = new PasswordParameters(); User owner = new User(); owner.setRole(UserRole.USER_NORMAL); params.setUser(owner); params.setApplication(app); params.setPasswordCreateDate(LocalDateTime.now(ZoneId.systemDefault()).minusDays(70));
        var policy = new PasswordPolicy(); policy.setApplication(app); policy.setExpirationEnabled(true); policy.setDefaultPolicy(true);
        when(passwordPolicyRepository.findAllWithExpirationEnabled()).thenReturn(List.of(policy));
        when(passwordParametersRepository.findByApplicationIds(Set.of(app.getId()))).thenReturn(List.of(params));
        assertEquals(List.of(params), userService.getUsersWithPasswordExpiringInDays(20));
        params.setPasswordCreateDate(LocalDateTime.now(ZoneId.systemDefault()).minusDays(91));
        userService.findUserToBlock();
        assertTrue(params.getBlockUser());
        verify(passwordParametersRepository).save(params);
    }

    @Test
    void doesNotWarnOrBlockWhenPoliciesAreDisabledOrAbsent() {
        when(passwordPolicyRepository.findAllWithExpirationEnabled()).thenReturn(List.of());
        assertTrue(userService.getUsersWithPasswordExpiringInDays(20).isEmpty());
        userService.findUserToBlock();
        verify(passwordParametersRepository, never()).findByApplicationIds(anyCollection());
    }

    @Test
    void validatesSuperUserAndRejectsNormalRole() {
        UUID tokenId = UUID.randomUUID(); Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("scope")).thenReturn(ScopeToken.REGISTER_TOKEN.getValue());
        when(jwt.getClaimAsString("token_log_id")).thenReturn(tokenId.toString());
        when(jwt.getSubject()).thenReturn("root@example.com"); when(tokenLogService.isTokenValid(tokenId)).thenReturn(true);
        User root = new User(); root.setRole(UserRole.SUPER_USER);
        when(userRepository.findByEmailWithApplications("root@example.com")).thenReturn(Optional.of(root));
        assertDoesNotThrow(() -> userService.validateAdminUser(jwt));
        root.setRole(UserRole.USER_NORMAL);
        assertThrows(ForbiddenOperationException.class, () -> userService.validateAdminUser(jwt));
    }

    @Test
    void directUserDoesNotNeedOauthApplicationAssociation() {
        Jwt jwt = mock(Jwt.class); when(jwt.getClaim("type_user")).thenReturn("DIRECT_USER");
        var response = userService.setApplicationToUser(jwt, APP_PUBLIC_ID);
        assertEquals("User is not OAuth, is not necessary registered", response.getMessage());
        verifyNoInteractions(applicationService, userRepository);
    }

    @Test
    void associatesOauthUserWithApplicationOnlyOnce() {
        UUID userId = UUID.randomUUID(); Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("type_user")).thenReturn("OAUTH_USER"); when(jwt.getClaim("user_id")).thenReturn(userId.toString());
        User oauth = new User(); oauth.setUserId(userId); oauth.setApplications(new java.util.HashSet<>());
        Application app = application();
        when(userRepository.findById(userId)).thenReturn(Optional.of(oauth));
        when(applicationService.findByPublicId(APP_PUBLIC_ID)).thenReturn(app);
        assertEquals("Application has been set for the user", userService.setApplicationToUser(jwt, APP_PUBLIC_ID).getMessage());
        assertTrue(oauth.getApplications().contains(app)); verify(userRepository).save(oauth);
        when(userRepository.findById(userId)).thenReturn(Optional.of(oauth));
        userService.setApplicationToUser(jwt, APP_PUBLIC_ID);
        verify(userRepository, times(1)).save(oauth);
    }

    @Test
    void returnsProfileAndUpdatesUsernameOnlyWhenChangedAndUnique() {
        Jwt jwt = mock(Jwt.class); when(jwt.getSubject()).thenReturn("test@example.com");
        User user = new User(); user.setEmail("test@example.com"); user.setUsername("old"); user.setFirstName("First"); user.setLastName("Last");
        when(userRepository.findByEmailWithApplications(user.getEmail())).thenReturn(Optional.of(user));
        var profile = userService.getAuthenticatedUserProfile(jwt);
        assertEquals("First Last", profile.getFullName()); assertEquals("old", profile.getUserName());
        var same = new UpdateUsernameRequest(); same.setUserName("old");
        userService.updateAuthenticatedUsername(jwt, same); verify(userRepository, never()).save(any());
        var changed = new UpdateUsernameRequest(); changed.setUserName(" new ");
        when(userRepository.existsByUsernameIgnoreCaseAndUserIdNot("new", user.getUserId())).thenReturn(false);
        userService.updateAuthenticatedUsername(jwt, changed);
        assertEquals("new", user.getUsername()); verify(userDataService).updateUserByEmailHeader(jwt, user.getEmail());
    }

    @Test
    void rejectsDuplicateUsername() {
        Jwt jwt = mock(Jwt.class); when(jwt.getSubject()).thenReturn("test@example.com");
        User user = new User(); user.setUserId(UUID.randomUUID()); user.setEmail("test@example.com"); user.setUsername("old");
        when(userRepository.findByEmailWithApplications(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameIgnoreCaseAndUserIdNot("taken", user.getUserId())).thenReturn(true);
        var request = new UpdateUsernameRequest(); request.setUserName("taken");
        assertThrows(br.com.hahn.auth.application.execption.ResourceAlreadyExistException.class, () -> userService.updateAuthenticatedUsername(jwt, request));
    }

}
