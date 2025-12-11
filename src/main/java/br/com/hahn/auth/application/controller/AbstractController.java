package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.application.execption.InvalidFormatTypeException;
import br.com.hahn.auth.application.execption.InvalidRecoverTokenException;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractController {

    public Jwt extractJwtFromContext(){
        log.info("AbstractController: Staring Jwt extraction from Context at: {}", Instant.now());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof Jwt)) {
            log.error("AbstractController: Not found token or token is invalid. Throw InvalidRecoverTokenException at: {}", Instant.now());
            throw new InvalidRecoverTokenException(ErrorsResponses.INVALID_RECOVERY_CODE.getMessage());
        }
        return (Jwt) auth.getPrincipal();
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
