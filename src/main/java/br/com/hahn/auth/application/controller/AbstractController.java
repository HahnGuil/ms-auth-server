package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.application.execption.InvalidFormatTypeException;
import br.com.hahn.auth.application.execption.InvalidRecoverTokenException;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractController {

    protected Jwt extractJwtFromContext() {
        log.info("AbstractController: Starting Jwt extraction from Context at: {}", Instant.now());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.error("AbstractController: Authentication is null. Throw InvalidRecoverTokenException at: {}", Instant.now());
            throw new InvalidRecoverTokenException(ErrorsResponses.INVALID_TOKEN.getMessage());
        }

        if (auth.getPrincipal() instanceof Jwt jwtPrincipal) {
            return jwtPrincipal;
        }

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }

        if (auth.getCredentials() instanceof Jwt jwtCredentials) {
            return jwtCredentials;
        }

        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                log.error("AbstractController: Authorization header presents but Spring could not convert to Jwt. Header start: {}... At: {}", header.substring(0, Math.min(header.length(), 60)), Instant.now());
                throw new InvalidRecoverTokenException(ErrorsResponses.FAIL_CONVERT_TOKEN.getMessage());
            }
        }

        String principalClass = auth.getPrincipal() == null ? "null" : auth.getPrincipal().getClass().getName();
        log.error("AbstractController: Expected primary type Jwt but found: {}. Throw InvalidRecoverTokenException at: {}", principalClass, Instant.now());
        throw new InvalidRecoverTokenException(ErrorsResponses.INVALID_TOKEN.getMessage());
    }

    public void validateEmailFormat(String email){
        log.info("AbstractController: Validating email: {} format at: {}", email, Instant.now());

        if (email == null || email.isBlank()) {
            log.error("AbstractController: Email is null or blank. Throw InvalidFormatTypeException at: {}", Instant.now());
            throw new InvalidFormatTypeException(ErrorsResponses.INVALID_EMAIL_FORMAT_TYPE.getMessage());
        }

        var emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
        if (!emailPattern.matcher(email).matches()) {
            log.error("AbstractController: Email format invalid for {}. Throw InvalidFormatException at: {}", email, Instant.now());
            throw new InvalidFormatTypeException(ErrorsResponses.INVALID_EMAIL_FORMAT_TYPE.getMessage());
        }
         log.info("AbstractController: Finish email validation at: {}", Instant.now());
    }

    public void validatePasswordFormat(String password) {
        log.info("AbstractController: Validating password format at: {}", Instant.now());

        if (password == null || password.isBlank()) {
            log.error("AbstractController: Password is null or blank. Throw InvalidFormatTypeException at: {}", Instant.now());
            throw new InvalidFormatTypeException(ErrorsResponses.INVALID_PASSWORD_FORMAT_TYPE.getMessage());
        }

        var passwordPattern = Pattern.compile("^(?=.*\\d)(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,12}$");
        if (!passwordPattern.matcher(password).matches()) {
            log.error("AbstractController: Password format invalid. Throw InvalidFormatTypeException at: {}", Instant.now());
            throw new InvalidFormatTypeException(ErrorsResponses.INVALID_PASSWORD_FORMAT_TYPE.getMessage());
        }

        log.info("AbstractController: Finish password format at: {}", Instant.now());
    }
}
