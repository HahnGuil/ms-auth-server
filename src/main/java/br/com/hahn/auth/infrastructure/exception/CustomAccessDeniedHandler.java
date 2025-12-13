package br.com.hahn.auth.infrastructure.exception;

import br.com.hahn.auth.domain.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * Handles an access denied exception by sending a JSON response with an error message and timestamp.
     *
     * @author HahnGuil
     * @param request the HTTP request that resulted in an AccessDeniedException
     * @param response the HTTP response to be sent to the client
     * @param accessDeniedException the exception that was thrown due to access being denied
     * @throws IOException if an input or output error occurs while handling the response
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ErrorResponse errorResponse = new ErrorResponse()
                .message("You don't have permission to access this operation")
                .timestamp(OffsetDateTime.now());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}