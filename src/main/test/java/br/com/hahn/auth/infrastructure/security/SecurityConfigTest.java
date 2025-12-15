package br.com.hahn.auth.infrastructure.security;

import br.com.hahn.auth.application.service.AuthService;
import br.com.hahn.auth.domain.model.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private SecurityFilter securityFilter;

    @Mock
    private AuthService authService;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Mock
    private HttpSecurity httpSecurity;

    @Mock
    private SecurityFilterChain securityFilterChain;

    @Test
    void shouldBuildSecurityFilterChainSuccessfully() throws Exception {
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.exceptionHandling(any())).thenReturn(httpSecurity);
        when(httpSecurity.oauth2Login(any())).thenReturn(httpSecurity);
        when(httpSecurity.oauth2ResourceServer(any())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenAnswer(_ -> securityFilterChain);

        SecurityFilterChain filterChain = securityConfig.securityFilterChain(httpSecurity);

        assertNotNull(filterChain);
        assertEquals(securityFilterChain, filterChain);
        verify(httpSecurity).csrf(any());
        verify(httpSecurity).sessionManagement(any());
        verify(httpSecurity).authorizeHttpRequests(any());
        verify(httpSecurity).exceptionHandling(any());
        verify(httpSecurity).oauth2Login(any());
        verify(httpSecurity).oauth2ResourceServer(any());
        verify(httpSecurity).addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
        verify(httpSecurity).build();
    }

    @Test
    void shouldReturnOAuth2UserServiceInstance() {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> userService = securityConfig.oAuth2UserService();

        assertNotNull(userService);
    }

    @Test
    void shouldReturnPasswordEncoderInstance() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        assertNotNull(passwordEncoder);
        assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder);
    }

    @Test
    void shouldReturnAuthenticationManagerInstance() throws Exception {
        AuthenticationConfiguration authenticationConfiguration = mock(AuthenticationConfiguration.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);

        assertNotNull(result);
        assertEquals(authenticationManager, result);
    }

    @Test
    void shouldHandleOAuth2AuthenticationSuccess() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);
        LoginResponse loginResponse = new LoginResponse()
                .userName("John Doe")
                .email("john.doe@example.com")
                .token("jwt-token-123")
                .refreshToken("refresh-token-456");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(authService.processOAuthUser(oAuth2User)).thenReturn(loginResponse);
        when(response.getWriter()).thenReturn(printWriter);

        AuthenticationSuccessHandler successHandler = securityConfig.oAuth2AuthenticationSuccessHandler();
        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(response).setContentType("application/json");
        verify(authService).processOAuthUser(oAuth2User);

        String jsonResponse = stringWriter.toString();
        assertFalse(jsonResponse.contains("\"userName\": \"John Doe\""));
        assertTrue(jsonResponse.contains("\"email\": \"john.doe@example.com\""));
        assertTrue(jsonResponse.contains("\"token\": \"jwt-token-123\""));
        assertTrue(jsonResponse.contains("\"refreshToken\": \"refresh-token-456\""));
    }
}