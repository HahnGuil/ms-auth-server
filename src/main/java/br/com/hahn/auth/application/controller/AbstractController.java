package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.application.execption.InvalidFormatTypeException;
import br.com.hahn.auth.application.execption.InvalidRecoverTokenException;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import br.com.hahn.auth.util.DateTimeConverter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractController {

    /**
     * Extracts a Jwt token from the current security context.
     * <p>
     * This method attempts to obtain a Jwt from the Spring Security context by:
     * - validating that an Authentication object is present,
     * - returning the principal when it is a {@link Jwt},
     * - returning the token when the authentication is a {@link org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken},
     * - returning the credentials when they are a {@link Jwt},
     * - as a fallback, inspecting the HTTP Authorization header: if a Bearer header is present
     *   but Spring could not convert it to a Jwt, a specific conversion exception is thrown.
     * If none of these yield a Jwt, an {@link br.com.hahn.auth.application.execption.InvalidRecoverTokenException}
     * is thrown indicating an invalid token.
     * </p>
     *
     * @author HahnGuil
     * @return the extracted {@link Jwt}
     * @throws br.com.hahn.auth.application.execption.InvalidRecoverTokenException when the Jwt cannot be recovered or converted
     */
    protected Jwt extractJwtFromContext() {
        log.info("AbstractController: Starting Jwt extraction from Context at: {}", DateTimeConverter.formatInstantNow());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.error("AbstractController: Authentication is null. Throw InvalidRecoverTokenException at: {}", DateTimeConverter.formatInstantNow());
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
                log.error("AbstractController: Authorization header presents but Spring could not convert to Jwt. Header start: {}... At: {}", header.substring(0, Math.min(header.length(), 60)), DateTimeConverter.formatInstantNow());
                throw new InvalidRecoverTokenException(ErrorsResponses.FAIL_CONVERT_TOKEN.getMessage());
            }
        }

        String principalClass = auth.getPrincipal() == null ? "null" : auth.getPrincipal().getClass().getName();
        log.error("AbstractController: Expected primary type Jwt but found: {}. Throw InvalidRecoverTokenException at: {}", principalClass, DateTimeConverter.formatInstantNow());
        throw new InvalidRecoverTokenException(ErrorsResponses.INVALID_TOKEN.getMessage());
    }

    /**
     * Validates the format of the provided email address.
     * <p>
     * This method checks if the email is not null or blank and matches the standard
     * email format pattern. If the email is invalid, an {@link InvalidFormatTypeException}
     * is thrown with an appropriate error message.
     * </p>
     *
     * @author HahnGuil
     * @param email the email address to be validated
     * @throws InvalidFormatTypeException if the email is null, blank, or does not match the expected format
     */
    public void validateEmailFormat(String email){
        log.info("AbstractController: Validating email: {} format at: {}", email, DateTimeConverter.formatInstantNow());

        if (email == null || email.isBlank()) {
            log.error("AbstractController: Email is null or blank. Throw InvalidFormatTypeException at: {}", DateTimeConverter.formatInstantNow());
            throw new InvalidFormatTypeException(ErrorsResponses.INVALID_EMAIL_FORMAT_TYPE.getMessage());
        }

        var emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
        if (!emailPattern.matcher(email).matches()) {
            log.error("AbstractController: Email format invalid for {}. Throw InvalidFormatException at: {}", email, DateTimeConverter.formatInstantNow());
            throw new InvalidFormatTypeException(ErrorsResponses.INVALID_EMAIL_FORMAT_TYPE.getMessage());
        }
         log.info("AbstractController: Finish email validation at: {}", DateTimeConverter.formatInstantNow());
    }

    /**
     * Validates the format of the provided password.
     * <p>
     * This method ensures that the password is not null or blank and matches the required format:
     * - Must contain at least one digit.
     * - Must contain at least one uppercase letter.
     * - Must contain at least one special character.
     * - Must be between 8 and 12 characters in length.
     * If the password is invalid, an {@link InvalidFormatTypeException} is thrown with an appropriate error message.
     * </p>
     *
     * @author HahnGuil
     * @param password the password to be validated
     * @throws InvalidFormatTypeException if the password is null, blank, or does not match the expected format
     */
    public void validatePasswordFormat(String password) {
        log.info("AbstractController: Validating password format at: {}", DateTimeConverter.formatInstantNow());

        if (password == null || password.isBlank()) {
            log.error("AbstractController: Password is null or blank. Throw InvalidFormatTypeException at: {}", DateTimeConverter.formatInstantNow());
            throw new InvalidFormatTypeException(ErrorsResponses.INVALID_PASSWORD_FORMAT_TYPE.getMessage());
        }

        var passwordPattern = Pattern.compile("^(?=.*\\d)(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,12}$");
        if (!passwordPattern.matcher(password).matches()) {
            log.error("AbstractController: Password format invalid. Throw InvalidFormatTypeException at: {}", DateTimeConverter.formatInstantNow());
            throw new InvalidFormatTypeException(ErrorsResponses.INVALID_PASSWORD_FORMAT_TYPE.getMessage());
        }

        log.info("AbstractController: Finish password format at: {}", DateTimeConverter.formatInstantNow());
    }
}
