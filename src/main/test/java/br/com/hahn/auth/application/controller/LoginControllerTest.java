package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.application.execption.InvalidFormatTypeException;
import br.com.hahn.auth.application.execption.InvalidRecoverTokenException;
import br.com.hahn.auth.application.service.AuthService;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import br.com.hahn.auth.domain.model.LogOfRequest;
import br.com.hahn.auth.domain.model.LoginRequest;
import br.com.hahn.auth.domain.model.LoginResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoginControllerTest {


    private final AuthService authService = mock(AuthService.class);
    private final LoginController controller = new LoginController(authService);

    @Test
    void shouldReturnCreatedStatusAndLoginResponseForValidLoginRequest() {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "Password1!");
        LoginResponse loginResponse = new LoginResponse();
        when(authService.userLogin(loginRequest)).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = controller.postLogin(loginRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(loginResponse, response.getBody());
        verify(authService, times(1)).userLogin(loginRequest);
    }

    @Test
    void shouldThrowExceptionForInvalidEmailFormatInLoginRequest() {
        LoginRequest loginRequest = new LoginRequest("invalid-email", "Password1!");

        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                controller.postLogin(loginRequest)
        );

        assertEquals(ErrorsResponses.INVALID_EMAIL_FORMAT_TYPE.getMessage(), exception.getMessage());
        verify(authService, never()).userLogin(any());
    }

    @Test
    void shouldThrowExceptionForInvalidPasswordFormatInLoginRequest() {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "weak");

        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                controller.postLogin(loginRequest)
        );

        assertEquals(ErrorsResponses.INVALID_PASSWORD_FORMAT_TYPE.getMessage(), exception.getMessage());
        verify(authService, never()).userLogin(any());
    }

    @Test
    void shouldReturnNoContentStatusForValidLogoffRequest() {
        LogOfRequest logOfRequest = new LogOfRequest("user@example.com");
        Jwt jwt = mock(Jwt.class);
        LoginController spyController = Mockito.spy(controller);
        Mockito.doReturn(jwt).when(spyController).extractJwtFromContext();

        ResponseEntity<Void> response = spyController.deleteLoggedUser(logOfRequest);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(authService, times(1)).logOffUser(jwt);
    }

    @Test
    void shouldThrowExceptionWhenJwtExtractionFailsDuringLogoff() {
        LogOfRequest logOfRequest = new LogOfRequest("user@example.com");
        LoginController spyController = Mockito.spy(controller);
        Mockito.doThrow(new InvalidRecoverTokenException(ErrorsResponses.INVALID_TOKEN.getMessage()))
                .when(spyController).extractJwtFromContext();

        InvalidRecoverTokenException exception = assertThrows(InvalidRecoverTokenException.class, () ->
                spyController.deleteLoggedUser(logOfRequest)
        );

        assertEquals(ErrorsResponses.INVALID_TOKEN.getMessage(), exception.getMessage());
        verify(authService, never()).logOffUser(any());
    }
}
