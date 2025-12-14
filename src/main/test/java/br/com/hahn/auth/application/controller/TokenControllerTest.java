package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.application.execption.InvalidFormatTypeException;
import br.com.hahn.auth.application.service.AuthService;
import br.com.hahn.auth.domain.model.LoginResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TokenControllerTest {

    private AuthService authService;
    private TokenController controller;

    @Test
    void shouldGenerateNewTokenSuccessfully() {
        authService = mock(AuthService.class);
        controller = new TokenController(authService);

        Jwt jwt = mock(Jwt.class);
        LoginResponse loginResponse = new LoginResponse();
        TokenController spyController = Mockito.spy(controller);
        Mockito.doReturn(jwt).when(spyController).extractJwtFromContext();
        when(authService.generateNewTokenForUser(jwt)).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = spyController.postRefreshToken();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(loginResponse, response.getBody());
        verify(authService, times(1)).generateNewTokenForUser(jwt);
    }

    @Test
    void shouldThrowExceptionWhenJwtIsInvalid() {
        authService = mock(AuthService.class);
        controller = new TokenController(authService);

        Jwt jwt = mock(Jwt.class);
        TokenController spyController = Mockito.spy(controller);
        Mockito.doReturn(jwt).when(spyController).extractJwtFromContext();
        doThrow(new InvalidFormatTypeException("Invalid JWT")).when(authService).generateNewTokenForUser(jwt);

        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, spyController::postRefreshToken
        );

        assertEquals("Invalid JWT", exception.getMessage());
        verify(authService, times(1)).generateNewTokenForUser(jwt);
    }
}
