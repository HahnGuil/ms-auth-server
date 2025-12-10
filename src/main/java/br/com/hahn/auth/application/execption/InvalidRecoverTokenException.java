package br.com.hahn.auth.application.execption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidRecoverTokenException extends RuntimeException {
    public InvalidRecoverTokenException(String message) {
        super(message);
    }
}
