package br.com.hahn.auth.presentation.exception;

import br.com.hahn.auth.application.dto.response.ErrorResponseDTO;
import br.com.hahn.auth.application.execption.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalControllerHandler {


    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<ErrorResponseDTO> handlerUserAlreadyExistForApplicationException(ResourceAlreadyExistException ex){
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataAccessException(DataAccessException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO("Error to process the request, try again" + ex, Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex){
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleApplicationNotFoundException(ApplicationNotFoundException ex){
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(UserNotFoundException ex){
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCredentialsException(InvalidCredentialsException ex){
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidFormatException(InvalidFormatException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(KeyRotationException.class)
    public ResponseEntity<ErrorResponseDTO> handleKeyRotationException(KeyRotationException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidOperationExecption.class)
    public ResponseEntity<ErrorResponseDTO> handleOperationException(InvalidOperationExecption ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserExistException(UserAlreadyExistException ex){
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DataBaseServerException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataBaseServerException(DataBaseServerException ex){
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(UserNotOAuthException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotOAuthException(UserNotOAuthException ex){
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(error);
    }

    @ExceptionHandler(ResetPasswordNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResetPasswordNotFoundException(ResetPasswordNotFoundException ex){
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UserEmailAlreadyExistException.class)
    public ResponseEntity<ErrorResponseDTO> handlerUserEmailAlreadyExistException(UserEmailAlreadyExistException ex){
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

}
