package br.com.hahn.auth.application.service;

import br.com.hahn.auth.domain.model.InvalidatedToken;
import br.com.hahn.auth.domain.respository.InvalidatedTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvalidatedTokenServiceTest {

    @Mock
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @InjectMocks
    private InvalidatedTokenService invalidatedTokenService;

    @Test
    void shouldSaveInvalidatedTokenSuccessfully() {
        InvalidatedToken invalidatedToken = new InvalidatedToken();
        invalidatedToken.setId(UUID.randomUUID());
        invalidatedToken.setUserId(UUID.randomUUID());

        invalidatedTokenService.save(invalidatedToken);

        verify(invalidatedTokenRepository, times(1)).save(invalidatedToken);
    }

    @Test
    void shouldThrowExceptionWhenInvalidatedTokenIsNull() {
        assertThrows(NullPointerException.class, () -> invalidatedTokenService.save(null));
        verifyNoInteractions(invalidatedTokenRepository);
    }
}
