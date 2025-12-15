package br.com.hahn.auth.infrastructure.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private AccessDeniedException mockAccessDeniedException;

    @InjectMocks
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Test
    void shouldSetForbiddenStatusAndWriteErrorResponse() throws IOException {
        StringWriter responseWriter = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

        customAccessDeniedHandler.handle(mockRequest, mockResponse, mockAccessDeniedException);

        verify(mockResponse).setContentType("application/json");
        verify(mockResponse).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(responseWriter.toString().contains("You don't have permission to access this operation"));
    }

    @Test
    void shouldHandleIOExceptionGracefully() throws IOException {
        when(mockResponse.getWriter()).thenThrow(new IOException());

        assertThrows(IOException.class, () ->
                customAccessDeniedHandler.handle(mockRequest, mockResponse, mockAccessDeniedException)
        );
    }
}