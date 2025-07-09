package br.com.hahn.auth.application.execption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class KeyRotationException extends RuntimeException {
    public KeyRotationException(String message) {
        super(message);
    }
}
