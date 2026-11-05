package br.com.hahn.auth.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {
    @Mock JwtDecoderConfig decoderConfig;
    @Mock JwtDecoder decoder;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain filterChain;
    @InjectMocks SecurityFilter filter;

    @BeforeEach void setUp() { SecurityContextHolder.clearContext(); }
    @AfterEach void tearDown() { SecurityContextHolder.clearContext(); }

    @Test
    void acceptsValidJwtAndContinuesFilterChain() throws Exception {
        Jwt jwt = Jwt.withTokenValue("validToken").header("alg", "none").claim("sub", "user@example.com").build();
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(decoderConfig.jwtDecoder()).thenReturn(decoder);
        when(decoder.decode("validToken")).thenReturn(jwt);
        filter.doFilterInternal(request, response, filterChain);
        verify(decoder).decode("validToken");
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void returnsUnauthorizedForInvalidJwt() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
        when(decoderConfig.jwtDecoder()).thenReturn(decoder);
        when(decoder.decode("invalidToken")).thenThrow(new JwtException("invalid"));
        filter.doFilterInternal(request, response, filterChain);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token, please log in to continue.");
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void continuesWithoutBearerToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(decoderConfig);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
