package br.com.hahn.auth.infrastructure.security;

import br.com.hahn.auth.application.execption.InvalidCredentialsException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;

    /**
     * Processes the HTTP request and applies security filtering.
     *
     * <p>This method extracts the authentication token from the request, validates it,
     * and sets the authentication in the SecurityContext if the token is valid. If the
     * token is invalid, an Unauthorized (401) error is returned. Finally, the request
     * is passed along the filter chain.</p>
     *
     * @author HahnGuil
     * @param request the HTTP request to be processed
     * @param response the HTTP response to be sent
     * @param filterChain the filter chain to pass the request and response to the next filter
     * @throws ServletException if an error occurs during the filtering process
     * @throws IOException if an I/O error occurs during the filtering process
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        var token = this.recoverToken(request);
        try {
            if (token != null) {
                String email = tokenService.validateToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (InvalidCredentialsException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }

    /**
     * Extracts the authentication token from the HTTP request.
     *
     * <p>This method retrieves the value of the "Authorization" header from the
     * provided HTTP request. If the header is present, it removes the "Bearer "
     * prefix from the token and returns the resulting string. If the header is
     * absent, it returns null.</p>
     *
     * @author HahnGuil
     * @param request the HTTP request containing the "Authorization" header
     * @return the extracted token without the "Bearer " prefix, or null if the header is absent
     */
    private String recoverToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}