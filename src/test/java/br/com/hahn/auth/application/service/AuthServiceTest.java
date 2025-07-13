package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.dto.request.UserRequestDTO;
import br.com.hahn.auth.application.dto.response.UserResponseDTO;
import br.com.hahn.auth.domain.model.User;
import br.com.hahn.auth.domain.respository.ResetPasswordRepository;
import br.com.hahn.auth.domain.respository.UserRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import br.com.hahn.auth.infrastructure.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ResetPasswordRepository resetPasswordRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, tokenService, emailService, resetPasswordRepository);
    }

    @Test
    void testCreateUser() {
        UserRequestDTO userRequestDTO = new UserRequestDTO("testUser", "test@example.com", "password", "Test", "User", null);

        UserResponseDTO userResponseDTO = authService.createUser(userRequestDTO);

        assertEquals("testUser", userResponseDTO.userName());
        assertEquals("test@example.com", userResponseDTO.email());

        User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
        assertNotNull(savedUser);
        assertEquals("testUser", savedUser.getUsername());
        assertTrue(passwordEncoder.matches("password", savedUser.getPassword()));
    }
}

