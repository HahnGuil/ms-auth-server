package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.request.UserRequestDTO;
import br.com.hahn.auth.application.execption.UserNotFoundException;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExistsByEmail() {
        String email = "test@example.com";

        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertTrue(userService.existsByEmail(email));
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    void testFindByEmail_Success() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.findByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testFindByEmail_NotFound() {
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findByEmail(email));
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testConvertToEntity() {
        UserRequestDTO dto = new UserRequestDTO("testUser", "test@example.com", "password", "Test", "User", "url", null);

        User user = userService.convertToEntity(dto, "encodedPassword");

        assertNotNull(user);
        assertEquals("testUser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("encodedPassword", user.getPassword());
    }

    @Test
    void testSaveUser() {
        User user = new User();
        user.setEmail("test@example.com");

        userService.saveUser(user);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdatePassword() {
        String email = "test@example.com";
        UUID id = UUID.randomUUID();
        String newPassword = "newPassword";
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 8, 1, 0, 6, 56, 283384000); // Data fixa

        userService.updatePassword(email, id, newPassword, fixedDateTime);

        verify(userRepository, times(1)).updatePasswordByEmailAndId(newPassword, email, id, fixedDateTime);
    }

    @Test
    void testConvertOAuthUserToRequestDTO() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("name")).thenReturn("testUser");
        when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
        when(oAuth2User.getAttribute("given_name")).thenReturn("Test");
        when(oAuth2User.getAttribute("family_name")).thenReturn("User");
        when(oAuth2User.getAttribute("picture")).thenReturn("url");

        UserRequestDTO dto = userService.convertOAuthUserToRequestDTO(oAuth2User);

        assertNotNull(dto);
        assertEquals("testUser", dto.userName());
        assertEquals("test@example.com", dto.email());
        assertEquals("Test", dto.firstName());
        assertEquals("User", dto.lastName());
        assertEquals("url", dto.pictureUrl());
    }
}

