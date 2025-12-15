package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.application.execption.InvalidTokenException;
import br.com.hahn.auth.application.execption.ResourceAlreadyExistException;
import br.com.hahn.auth.application.execption.UserNotFoundException;
import br.com.hahn.auth.domain.model.ErrorResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GlobalControllerHandlerTest {

    private final GlobalControllerHandler handler = new GlobalControllerHandler();

    @Nested
    @DisplayName("Handle ResourceAlreadyExistException")
    class HandleResourceAlreadyExistException {

        @Test
        @DisplayName("Returns CONFLICT status with correct error message")
        void returnsConflictStatus() {
            ResourceAlreadyExistException exception = new ResourceAlreadyExistException("Resource already exists");

            ResponseEntity<ErrorResponse> response = handler.handlerUserAlreadyExistForApplicationException(exception);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            Assertions.assertNotNull(response.getBody());
            assertEquals("Resource already exists", response.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("Handle DataAccessException")
    class HandleDataAccessException {

        @Test
        @DisplayName("Returns INTERNAL_SERVER_ERROR status with correct error message")
        void returnsInternalServerErrorStatus() {
            DataAccessException exception = new DataAccessException("Database error") {};

            ResponseEntity<ErrorResponse> response = handler.handleDataAccessException(exception);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            Assertions.assertNotNull(response.getBody());
            assertEquals("Error to process the request, try againDatabase error", response.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("Handle MethodArgumentNotValidException")
    class HandleMethodArgumentNotValidException {

        @Test
        @DisplayName("Returns UNPROCESSABLE_ENTITY status with correct error message")
        void returnsUnprocessableEntityStatus() {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

            ResponseEntity<ErrorResponse> response = handler.handleValidation(exception);

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Handle InvalidTokenException")
    class HandleInvalidTokenException {

        @Test
        @DisplayName("Returns UNAUTHORIZED status with correct error message")
        void returnsUnauthorizedStatus() {
            InvalidTokenException exception = new InvalidTokenException("Invalid token");

            ResponseEntity<ErrorResponse> response = handler.handlerInvalidTokenException(exception);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            Assertions.assertNotNull(response.getBody());
            assertEquals("Invalid token", response.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("Handle UserNotFoundException")
    class HandleUserNotFoundException {

        @Test
        @DisplayName("Returns NOT_FOUND status with correct error message")
        void returnsNotFoundStatus() {
            UserNotFoundException exception = new UserNotFoundException("User not found");

            ResponseEntity<ErrorResponse> response = handler.handleUserNotFoundException(exception);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            Assertions.assertNotNull(response.getBody());
            assertEquals("User not found", response.getBody().getMessage());
        }
    }
}