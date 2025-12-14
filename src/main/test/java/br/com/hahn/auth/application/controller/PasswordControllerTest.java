package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.application.execption.InvalidFormatTypeException;
import br.com.hahn.auth.application.service.PasswordService;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import br.com.hahn.auth.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PasswordControllerTest {

    private final String oldPassword = "#Password24";
    private final String newPassword = "#Password25";
    private final String incorrectPasswordFormat = "weak";
    private final String correctEmailFormat = "user@email.com";
    private final String incorrectEmailFormat = "user.email.com";
    private PasswordService passwordService;
    private PasswordController controller;


    @BeforeEach
    void setUp() {
        passwordService = mock(PasswordService.class);
        controller = new PasswordController(passwordService);
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        ChangePasswordRequest request = new ChangePasswordRequest(correctEmailFormat, oldPassword, newPassword);
        Jwt jwt = mock(Jwt.class);
        PasswordController spyController = Mockito.spy(controller);
        Mockito.doReturn(jwt).when(spyController).extractJwtFromContext();

        ResponseEntity<Void> response = spyController.patchChangePassword(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        Mockito.verify(passwordService, times(1)).validateTokenForChangePassword(jwt);
        Mockito.verify(passwordService, times(1)).changePassword(request);
    }

    @Test
    void shouldThrowExceptionForInvalidEmailInChangePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest(incorrectEmailFormat, oldPassword, newPassword);

        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                controller.patchChangePassword(request)
        );

        assertEquals(ErrorsResponses.INVALID_EMAIL_FORMAT_TYPE.getMessage(), exception.getMessage());
        Mockito.verify(passwordService, never()).changePassword(any());
    }

    @Test
    void shouldThrowExceptionForInvalidNewPasswordInChangePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest(correctEmailFormat, oldPassword, incorrectPasswordFormat);

        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                controller.patchChangePassword(request)
        );

        assertEquals(ErrorsResponses.INVALID_PASSWORD_FORMAT_TYPE.getMessage(), exception.getMessage());
        Mockito.verify(passwordService, never()).changePassword(any());
    }

    @Test
    void shouldThrowExceptionForInvalidOldPasswordInChangePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest(correctEmailFormat, incorrectPasswordFormat, newPassword);

        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                controller.patchChangePassword(request)
        );

        assertEquals(ErrorsResponses.INVALID_PASSWORD_FORMAT_TYPE.getMessage(), exception.getMessage());
        Mockito.verify(passwordService, never()).changePassword(any());
    }

    @Test
    void shouldThrowExceptionWhenTokenIsInvalidForChangePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest(correctEmailFormat, oldPassword, newPassword);
        Jwt jwt = mock(Jwt.class);
        PasswordController spyController = Mockito.spy(controller);
        Mockito.doReturn(jwt).when(spyController).extractJwtFromContext();
        doThrow(new InvalidFormatTypeException(ErrorsResponses.INVALID_TOKEN.getMessage()))
                .when(passwordService).validateTokenForChangePassword(jwt);

        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                spyController.patchChangePassword(request)
        );

        assertEquals(ErrorsResponses.INVALID_TOKEN.getMessage(), exception.getMessage());
        Mockito.verify(passwordService, times(1)).validateTokenForChangePassword(jwt);
        Mockito.verify(passwordService, never()).changePassword(any());
    }

    @Test
    void shouldResetPasswordSuccessfully() {
        String password = "#Password01";
        NewPasswordRequest request = new NewPasswordRequest(password);
        Jwt jwt = mock(Jwt.class);
        PasswordController spyController = Mockito.spy(controller);
        Mockito.doReturn(jwt).when(spyController).extractJwtFromContext();

        ResponseEntity<Void> response = spyController.patchResetPassword(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        Mockito.verify(passwordService, times(1)).resetUserPassword(jwt, request);
    }

    @Test
    void shouldThrowExceptionWhenTokenIsInvalidForResetPassword() {
        NewPasswordRequest request = new NewPasswordRequest(newPassword);
        Jwt jwt = mock(Jwt.class);
        PasswordController spyController = Mockito.spy(controller);
        Mockito.doReturn(jwt).when(spyController).extractJwtFromContext();
        doThrow(new InvalidFormatTypeException(ErrorsResponses.INVALID_TOKEN.getMessage()))
                .when(passwordService).resetUserPassword(jwt, request);

        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                spyController.patchResetPassword(request)
        );

        assertEquals(ErrorsResponses.INVALID_TOKEN.getMessage(), exception.getMessage());
        Mockito.verify(passwordService, times(1)).resetUserPassword(jwt, request);
    }

    @Test
    void shouldThrowExceptionForInvalidPasswordInResetPassword() {
        NewPasswordRequest request = new NewPasswordRequest(incorrectPasswordFormat);

        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                controller.patchResetPassword(request)
        );

        assertEquals(ErrorsResponses.INVALID_PASSWORD_FORMAT_TYPE.getMessage(), exception.getMessage());
        Mockito.verify(passwordService, never()).resetUserPassword(any(), any());
    }

    @Test
    void shouldGenerateValidationCodeSuccessfully() {
        PasswordResetRequest request = new PasswordResetRequest(correctEmailFormat);
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setMessage("Code sent successfully");
        when(passwordService.requestValidateCode(request)).thenReturn(successResponse);

        ResponseEntity<SuccessResponse> response = controller.postPasswordResetRequest(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successResponse, response.getBody());
        Mockito.verify(passwordService, times(1)).requestValidateCode(request);
    }

    @Test
    void shouldThrowExceptionForInvalidEmailInPasswordResetRequest() {
        PasswordResetRequest request = new PasswordResetRequest(incorrectEmailFormat);

        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                controller.postPasswordResetRequest(request)
        );

        assertEquals(ErrorsResponses.INVALID_EMAIL_FORMAT_TYPE.getMessage(), exception.getMessage());
        Mockito.verify(passwordService, never()).requestValidateCode(any());
    }

    @Test
    void shouldThrowExceptionForEmptyEmailInPasswordResetRequest() {
        PasswordResetRequest request = new PasswordResetRequest("");

        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                controller.postPasswordResetRequest(request)
        );

        assertEquals(ErrorsResponses.INVALID_EMAIL_FORMAT_TYPE.getMessage(), exception.getMessage());
        Mockito.verify(passwordService, never()).requestValidateCode(any());
    }

    @Test
    void shouldValidateRecoveryCodeSuccessfully() {
        ValidateCodeRequest request = new ValidateCodeRequest(correctEmailFormat, "123456");
        ValidateCodeResponse responseMock = new ValidateCodeResponse();
        when(passwordService.validateResetCode(request)).thenReturn(responseMock);

        ResponseEntity<ValidateCodeResponse> response = controller.postRequestValidateCode(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseMock, response.getBody());
        Mockito.verify(passwordService, times(1)).validateResetCode(request);
    }
}
