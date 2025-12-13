package br.com.hahn.auth.presentation.exception;

import br.com.hahn.auth.application.execption.*;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import br.com.hahn.auth.domain.model.ErrorResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Global exception handler for controllers.
 *
 * <p>Centralizes mapping of application and framework exceptions to HTTP responses.
 * Each handled exception is converted to an {@code ErrorResponse} containing
 * a descriptive message and a UTC timestamp.</p>
 *
 * <p>The class is intended to be used as a {@code @ControllerAdvice} and provides
 * {@code @ExceptionHandler} methods for various domain exceptions (for example:
 * resource not found, invalid credentials, invalid tokens, validation errors, and
 * database access errors), translating them into appropriate HTTP status codes.</p>
 *
 * @author HahnGuil
 */
@ControllerAdvice
public class GlobalControllerHandler {


    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handlerUserAlreadyExistForApplicationException(ResourceAlreadyExistException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
        var error = generateErrorResponse("Error to process the request, try again" + ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleApplicationNotFoundException(ApplicationNotFoundException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormatException(InvalidFormatException ex) {
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(KeyRotationException.class)
    public ResponseEntity<ErrorResponse> handleKeyRotationException(KeyRotationException ex) {
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleOperationException(InvalidOperationException ex) {
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleUserExistException(UserAlreadyExistException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DataBaseServerException.class)
    public ResponseEntity<ErrorResponse> handleDataBaseServerException(DataBaseServerException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(UserNotOAuthException.class)
    public ResponseEntity<ErrorResponse> handleUserNotOAuthException(UserNotOAuthException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(error);
    }

    @ExceptionHandler(ResetPasswordNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResetPasswordNotFoundException(ResetPasswordNotFoundException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UserEmailAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handlerUserEmailAlreadyExistException(UserEmailAlreadyExistException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(UserCanNotChangePasswordException.class)
    public ResponseEntity<ErrorResponse> handlerUserCanNotChangePasswordException(UserCanNotChangePasswordException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(NotFoundResetPasswordRequestForUser.class)
    public ResponseEntity<ErrorResponse> handlerNotFoundResetPasswordRequestForUser(NotFoundResetPasswordRequestForUser ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(InvalidRecoverTokenException.class)
    public ResponseEntity<ErrorResponse> handlerInvalidRecoverTokenException(InvalidRecoverTokenException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handlerInvalidRefreshTokenException(InvalidRefreshTokenException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handlerInvalidTokenException(InvalidTokenException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InvalidFormatTypeException.class)
    public ResponseEntity<ErrorResponse> handlerInvalidFormatTypeException(InvalidFormatTypeException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(UserAlreadyLoggedInException.class)
    public ResponseEntity<ErrorResponse> handlerUserAlreadyLoggedInException(UserAlreadyLoggedInException ex){
        var error = generateErrorResponse(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var message = ErrorsResponses.INVALID_FORMAT_ON_REQUEST.getMessage();
        var error = generateErrorResponse(ex + message, Instant.now());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    private ErrorResponse generateErrorResponse(String message, Instant timeStamp){
        var errorResponse = new ErrorResponse();
        errorResponse.setMessage(message);
        errorResponse.setTimestamp(
                OffsetDateTime.ofInstant(timeStamp, ZoneOffset.UTC)
        );
        return errorResponse;
    }
}
