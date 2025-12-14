package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.application.execption.InvalidFormatTypeException;
import br.com.hahn.auth.application.service.UserService;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import br.com.hahn.auth.domain.model.UserRequest;
import br.com.hahn.auth.domain.model.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserController controller;
    private UserService userService;

    @Test
    void shouldRegisterUserSuccessfully() {
        userService = mock(UserService.class);
        controller = new UserController(userService);

        var userRequest = createUserRequest();
        UserResponse userResponse = new UserResponse();
        when(userService.createUser(userRequest)).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = controller.postRegisterUser(userRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userResponse, response.getBody());
        verify(userService, times(1)).createUser(userRequest);
    }

    @Test
    void shouldThrowExceptionForInvalidEmailFormat() {
        userService = mock(UserService.class);
        controller = new UserController(userService);

        var userRequest = createUserRequest();
        userRequest.setEmail("user.email.com");

        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                controller.postRegisterUser(userRequest)
        );

        assertEquals(ErrorsResponses.INVALID_EMAIL_FORMAT_TYPE.getMessage(), exception.getMessage());
        verify(userService, never()).createUser(any());
    }

    @Test
    void shouldThrowExceptionForInvalidPasswordFormat() {
        userService = mock(UserService.class);
        controller = new UserController(userService);

        var userRequest = createUserRequest();
        userRequest.setPassword("weak");

        InvalidFormatTypeException exception = assertThrows(InvalidFormatTypeException.class, () ->
                controller.postRegisterUser(userRequest)
        );

        assertEquals(ErrorsResponses.INVALID_PASSWORD_FORMAT_TYPE.getMessage(), exception.getMessage());
        verify(userService, never()).createUser(any());
    }

    private UserRequest createUserRequest(){
        UserRequest request = new UserRequest();
        request.setUsername("JohnDoe");
        request.setEmail("user@email.com");
        request.setPassword("#Password25");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setTypeUser(UserRequest.TypeUserEnum.DIRECT_USER);
        request.setPictureUrl("URL-PHOTO");
        request.setApplicationCode(1L);

        return request;
    }
}
