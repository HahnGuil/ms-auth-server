package br.com.hahn.auth.application.execption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidEmailNotFoundException extends RuntimeException {
    public InvalidEmailNotFoundException(String message) {
        super(message);
    }
}
