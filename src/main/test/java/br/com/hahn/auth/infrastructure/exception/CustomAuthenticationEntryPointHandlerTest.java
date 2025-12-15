package br.com.hahn.auth.infrastructure.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointHandlerTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private AuthenticationException mockAuthException;

    @InjectMocks
    private CustomAuthenticationEntryPointHandler entryPointHandler;

    @Test
    void shouldSetUnauthorizedStatusAndWriteErrorResponse() throws IOException {
        StringWriter responseWriter = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

        entryPointHandler.commence(mockRequest, mockResponse, mockAuthException);

        verify(mockResponse).setContentType("application/json");
        verify(mockResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("User not authenticated. Please log to continue"));
    }

    @Test
    void shouldHandleIOExceptionGracefullyWhenWritingResponse() throws IOException {
        when(mockResponse.getWriter()).thenThrow(new IOException());

        assertThrows(IOException.class, () ->
                entryPointHandler.commence(mockRequest, mockResponse, mockAuthException)
        );
    }
}