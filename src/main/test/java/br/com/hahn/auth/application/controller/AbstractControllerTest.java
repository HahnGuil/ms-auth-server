package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.application.execption.InvalidFormatTypeException;
import br.com.hahn.auth.application.execption.InvalidRecoverTokenException;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractControllerTest {
    private final AbstractController controller = new AbstractController() {};

    @Test
    void shouldExtractJwtFromJwtPrincipal() {
        Jwt jwt = mock(Jwt.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        Jwt result = controller.extractJwtFromContext();

        assertEquals(jwt, result);
    }

    @Test
    void shouldExtractJwtFromJwtAuthenticationToken() {
        Jwt jwt = mock(Jwt.class);
        JwtAuthenticationToken jwtAuth = mock(JwtAuthenticationToken.class);
        when(jwtAuth.getToken()).thenReturn(jwt);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(jwtAuth);
        SecurityContextHolder.setContext(securityContext);

        Jwt result = controller.extractJwtFromContext();

        assertEquals(jwt, result);
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationIsNull() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        InvalidRecoverTokenException exception = assertThrows(InvalidRecoverTokenException.class, controller::extractJwtFromContext
        );

        assertEquals(ErrorsResponses.INVALID_TOKEN.getMessage(), exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForInvalidAuthorizationHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        when(attributes.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attributes);

        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        InvalidRecoverTokenException exception = assertThrows(InvalidRecoverTokenException.class, controller::extractJwtFromContext
        );

        assertEquals(ErrorsResponses.FAIL_CONVERT_TOKEN.getMessage(), exception.getMessage());
    }

    @Test
    void shouldValidateCorrectEmailFormat() {
        controller.validateEmailFormat("test@example.com");
    }

    @Test
    void shouldThrowExceptionForNullEmail() {
        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                controller.validateEmailFormat(null)
        );

        assertEquals(ErrorsResponses.INVALID_EMAIL_FORMAT_TYPE.getMessage(), exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForInvalidEmailFormat() {
        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                controller.validateEmailFormat("invalid-email")
        );

        assertEquals(ErrorsResponses.INVALID_EMAIL_FORMAT_TYPE.getMessage(), exception.getMessage());
    }

    @Test
    void shouldValidateCorrectPasswordFormat() {
        controller.validatePasswordFormat("Password1!");
    }

    @Test
    void shouldThrowExceptionForNullPassword() {
        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                controller.validatePasswordFormat(null)
        );

        assertEquals(ErrorsResponses.INVALID_PASSWORD_FORMAT_TYPE.getMessage(), exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForInvalidPasswordFormat() {
        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                controller.validatePasswordFormat("weak")
        );

        assertEquals(ErrorsResponses.INVALID_PASSWORD_FORMAT_TYPE.getMessage(), exception.getMessage());
    }
}
