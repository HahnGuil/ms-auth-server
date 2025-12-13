package br.com.hahn.auth.infrastructure.exception;

import br.com.hahn.auth.domain.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class CustomAuthenticationEntryPointHandler implements AuthenticationEntryPoint {

    /**
     * Handles an authentication exception by sending a JSON response with an error message and timestamp.
     *
     * @author HahnGuil
     * @param request the HTTP request that triggered the AuthenticationException
     * @param response the HTTP response to be sent to the client
     * @param authException the exception that was thrown due to authentication failure
     * @throws IOException if an input or output error occurs while handling the response
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse errorResponse = new ErrorResponse()
                .message("User not authenticated. Please log to continue")
                .timestamp(OffsetDateTime.now());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}