package br.com.hahn.auth.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class JwtDecoderConfigTest {

    @Mock
    private KeyManager keyManager;


    @InjectMocks
    private JwtDecoderConfig jwtDecoderConfig;

    @Test
    void shouldCreateJwtDecoderBean() {
        JwtDecoder decoder = jwtDecoderConfig.jwtDecoder();

        assertNotNull(decoder);
    }

    @Test
    void shouldThrowJwtExceptionForInvalidTokenFormat() {
        String invalidToken = "invalidToken";

        JwtDecoder decoder = jwtDecoderConfig.jwtDecoder();

        assertThrows(JwtException.class, () -> decoder.decode(invalidToken));
    }

    @Test
    void shouldThrowJwtExceptionWhenNoValidPublicKeyAvailable() {
        String validHeader = Base64.getEncoder().encodeToString("{}".getBytes());
        String token = validHeader + ".payload.signature";
        when(keyManager.getCurrentKeyPair()).thenReturn(null);

        JwtDecoder decoder = jwtDecoderConfig.jwtDecoder();

        assertThrows(JwtException.class, () -> decoder.decode(token));
        verify(keyManager).getCurrentKeyPair();
    }

    @Test
    void shouldThrowJwtExceptionForDecodingError() {
        String validHeader = Base64.getEncoder().encodeToString("{\"kid\":\"testKid\"}".getBytes());
        String token = validHeader + ".payload.signature";
        when(keyManager.getPublicKeys()).thenThrow(new RuntimeException("Decoding error"));

        JwtDecoder decoder = jwtDecoderConfig.jwtDecoder();

        assertThrows(JwtException.class, () -> decoder.decode(token));
        verify(keyManager).getPublicKeys();
    }
}