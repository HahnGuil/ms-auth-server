package br.com.hahn.auth.application.exception;

import br.com.hahn.auth.application.execption.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExceptionsTest {

    @Test
    void shouldCreateDirectLoginNotAllowedExceptionWithMessage() {
        String msg = "Direct login not allowed";
        DirectLoginNotAllowedException ex = new DirectLoginNotAllowedException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveUnprocessableEntityResponseStatusForDirectLoginNotAllowedException() {
        ResponseStatus rs = DirectLoginNotAllowedException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, rs.value());
    }

    @Test
    void shouldCreateInvalidCredentialsExceptionWithMessage() {
        String msg = "Invalid credentials";
        InvalidCredentialsException ex = new InvalidCredentialsException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveUnauthorizedResponseStatusForInvalidCredentialsException() {
        ResponseStatus rs = InvalidCredentialsException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.UNAUTHORIZED, rs.value());
    }

    @Test
    void shouldCreateInvalidFormatExceptionWithMessage() {
        String msg = "Invalid format";
        InvalidFormatException ex = new InvalidFormatException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveBadGatewayResponseStatusForInvalidFormatException() {
        ResponseStatus rs = InvalidFormatException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.BAD_GATEWAY, rs.value());
    }

    @Test
    void shouldCreateInvalidFormatTypeExceptionWithMessage() {
        String msg = "Invalid format type";
        InvalidFormatTypeException ex = new InvalidFormatTypeException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveUnprocessableEntityResponseStatusForInvalidFormatTypeException() {
        ResponseStatus rs = InvalidFormatTypeException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, rs.value());
    }

    @Test
    void shouldCreateInvalidOperationExceptionWithMessage() {
        String msg = "Invalid operation";
        InvalidOperationException ex = new InvalidOperationException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveBadRequestResponseStatusForInvalidOperationException() {
        ResponseStatus rs = InvalidOperationException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.BAD_REQUEST, rs.value());
    }

    @Test
    void shouldCreateInvalidRecoverCodeExceptionWithMessage() {
        String msg = "Invalid recover code";
        InvalidRecoverCodeException ex = new InvalidRecoverCodeException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveUnauthorizedResponseStatusForInvalidRecoverCodeException() {
        ResponseStatus rs = InvalidRecoverCodeException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.UNAUTHORIZED, rs.value());
    }

    @Test
    void shouldCreateInvalidRecoverTokenExceptionWithMessage() {
        String msg = "Invalid recover token";
        InvalidRecoverTokenException ex = new InvalidRecoverTokenException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveUnauthorizedResponseStatusForInvalidRecoverTokenException() {
        ResponseStatus rs = InvalidRecoverTokenException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.UNAUTHORIZED, rs.value());
    }

    @Test
    void shouldCreateInvalidRefreshTokenExceptionWithMessage() {
        String msg = "Invalid refresh token";
        InvalidRefreshTokenException ex = new InvalidRefreshTokenException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveUnauthorizedResponseStatusForInvalidRefreshTokenException() {
        ResponseStatus rs = InvalidRefreshTokenException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.UNAUTHORIZED, rs.value());
    }

    @Test
    void shouldCreateInvalidTokenExceptionWithMessage() {
        String msg = "Invalid token";
        InvalidTokenException ex = new InvalidTokenException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveUnauthorizedResponseStatusForInvalidTokenException() {
        ResponseStatus rs = InvalidTokenException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.UNAUTHORIZED, rs.value());
    }

    @Test
    void shouldCreateKeyRotationExceptionWithMessage() {
        String msg = "Key rotation error";
        KeyRotationException ex = new KeyRotationException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveBadRequestResponseStatusForKeyRotationException() {
        ResponseStatus rs = KeyRotationException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.BAD_REQUEST, rs.value());
    }

    @Test
    void shouldCreateNotFoundResetPasswordRequestForUserWithMessage() {
        String msg = "Reset password request not found for user";
        NotFoundResetPasswordRequestForUser ex = new NotFoundResetPasswordRequestForUser(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveNotFoundResponseStatusForNotFoundResetPasswordRequestForUser() {
        ResponseStatus rs = NotFoundResetPasswordRequestForUser.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.NOT_FOUND, rs.value());
    }

    @Test
    void shouldCreateResetPasswordNotFoundExceptionWithMessage() {
        String msg = "Reset password not found";
        ResetPasswordNotFoundException ex = new ResetPasswordNotFoundException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveNotFoundResponseStatusForResetPasswordNotFoundException() {
        ResponseStatus rs = ResetPasswordNotFoundException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.NOT_FOUND, rs.value());
    }

    @Test
    void shouldCreateResourceAlreadyExistExceptionWithMessage() {
        String msg = "Resource already exists";
        ResourceAlreadyExistException ex = new ResourceAlreadyExistException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveConflictResponseStatusForResourceAlreadyExistException() {
        ResponseStatus rs = ResourceAlreadyExistException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.CONFLICT, rs.value());
    }

    @Test
    void shouldCreateResourceNotFoundExceptionWithMessage() {
        String msg = "Resource not found";
        ResourceNotFoundException ex = new ResourceNotFoundException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveConflictResponseStatusForResourceNotFoundException() {
        ResponseStatus rs = ResourceNotFoundException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.CONFLICT, rs.value());
    }

    @Test
    void shouldCreateUserAlreadyExistExceptionWithMessage() {
        String msg = "User already exists";
        UserAlreadyExistException ex = new UserAlreadyExistException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveConflictResponseStatusForUserAlreadyExistException() {
        ResponseStatus rs = UserAlreadyExistException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.CONFLICT, rs.value());
    }

    @Test
    void shouldCreateUserAlreadyLoggedInExceptionWithMessage() {
        String msg = "User already logged in";
        UserAlreadyLoggedInException ex = new UserAlreadyLoggedInException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveConflictResponseStatusForUserAlreadyLoggedInException() {
        ResponseStatus rs = UserAlreadyLoggedInException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.CONFLICT, rs.value());
    }

    @Test
    void shouldCreateUserBlockExceptionWithMessage() {
        String msg = "User blocked";
        UserBlockException ex = new UserBlockException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveUnauthorizedResponseStatusForUserBlockException() {
        ResponseStatus rs = UserBlockException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.UNAUTHORIZED, rs.value());
    }

    @Test
    void shouldCreateUserCanNotChangePasswordExceptionWithMessage() {
        String msg = "User cannot change password";
        UserCanNotChangePasswordException ex = new UserCanNotChangePasswordException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveUnprocessableEntityResponseStatusForUserCanNotChangePasswordException() {
        ResponseStatus rs = UserCanNotChangePasswordException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, rs.value());
    }

    @Test
    void shouldCreateUserEmailAlreadyExistExceptionWithMessage() {
        String msg = "User email already exists";
        UserEmailAlreadyExistException ex = new UserEmailAlreadyExistException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveConflictResponseStatusForUserEmailAlreadyExistException() {
        ResponseStatus rs = UserEmailAlreadyExistException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.CONFLICT, rs.value());
    }

    @Test
    void shouldCreateUserNotFoundExceptionWithMessage() {
        String msg = "User not found";
        UserNotFoundException ex = new UserNotFoundException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveNotFoundResponseStatusForUserNotFoundException() {
        ResponseStatus rs = UserNotFoundException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.NOT_FOUND, rs.value());
    }

    @Test
    void shouldCreateUserNotOAuthExceptionWithMessage() {
        String msg = "User not OAuth";
        UserNotOAuthException ex = new UserNotOAuthException(msg);
        assertEquals(msg, ex.getMessage());
    }

    @Test
    void shouldHaveNotAcceptableResponseStatusForUserNotOAuthException() {
        ResponseStatus rs = UserNotOAuthException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(rs);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, rs.value());
    }

    @Test
    void shouldCreateApplicationNotFoundExceptionWithMessage() {
        String message = "Application not found";
        ApplicationNotFoundException exception = new ApplicationNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowApplicationNotFoundExceptionWithCorrectHttpStatus() {
        ResponseStatus responseStatus = ApplicationNotFoundException.class.getAnnotation(ResponseStatus.class);

        assertNotNull(responseStatus);
        assertEquals(HttpStatus.NOT_FOUND, responseStatus.value());
    }

    @Test
    void shouldCreateDataBaseServerExceptionWithMessage() {
        String message = "Database server is down";
        DataBaseServerException exception = new DataBaseServerException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowDataBaseServerExceptionWithCorrectHttpStatus() {
        ResponseStatus responseStatus = DataBaseServerException.class.getAnnotation(ResponseStatus.class);

        assertNotNull(responseStatus);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseStatus.value());
    }

}
