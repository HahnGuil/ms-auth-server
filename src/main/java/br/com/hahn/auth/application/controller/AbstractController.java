package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.application.execption.InvalidRecoverTokenException;
import br.com.hahn.auth.domain.enums.ErrorsResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

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
}
