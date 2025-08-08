package br.com.hahn.auth.presentation.exception;

import br.com.hahn.auth.application.execption.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalControllerHandler {

    private static final String ERROR_MESSAGE = "error";

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<Map<String, String>> userAlreadyExistForApplicationException(ResourceAlreadyExistException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDataAccessException(DataAccessException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error to process the request, try again");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>>handleResourceNotFoundException(ResourceNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<Map<String, String>>handleApplicationNotFoundExecption(ApplicationNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>>handleUserNotFoundExecption(UserNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentialsException(InvalidCredentialsException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFormatException(InvalidFormatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(KeyRotationException.class)
    public ResponseEntity<Map<String, String>> handleKeyRotationException(KeyRotationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(InvalidOperationExecption.class)
    public ResponseEntity<Map<String, String>> handleOperationException(InvalidOperationExecption ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<Map<String, String>> handleUserExistException(UserAlreadyExistException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

}
