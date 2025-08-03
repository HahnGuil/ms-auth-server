package br.com.hahn.auth.application.authentication;

import br.com.hahn.auth.application.dto.request.LoginRequestDTO;
import br.com.hahn.auth.application.dto.request.UserRequestDTO;
import br.com.hahn.auth.application.dto.response.LoginResponseDTO;
import br.com.hahn.auth.application.dto.response.UserResponseDTO;
import br.com.hahn.auth.application.service.AuthService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final AuthController authController = new AuthController(authService);

    @Test
    void testRegister() {
        UserRequestDTO userRequestDTO = new UserRequestDTO("testUser", "test@example.com", "password", "Test", "User", "", null);
        UserResponseDTO userResponseDTO = new UserResponseDTO("testUser", "test@example.com");

        when(authService.createUser(userRequestDTO)).thenReturn(userResponseDTO);

        ResponseEntity<Map<String, Object>> response = authController.register(userRequestDTO);

        assertEquals(201, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        assertEquals("User successfully registered", response.getBody().get("message"));
        assertEquals(userResponseDTO, response.getBody().get("user"));

        verify(authService, times(1)).existsUserByEmail(userRequestDTO.email());
        verify(authService, times(1)).createUser(userRequestDTO);
    }

    @Test
    void testLogin() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("test@example.com", "password");
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO("test@example.com", "token", "refreshToken");

        when(authService.userLogin(loginRequestDTO)).thenReturn(loginResponseDTO);

        ResponseEntity<LoginResponseDTO> response = authController.login(loginRequestDTO);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(loginResponseDTO, response.getBody());

        verify(authService, times(1)).userLogin(loginRequestDTO);
    }
}

