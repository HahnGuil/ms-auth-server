package br.com.hahn.auth.application.execption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResetCodeAlreadyExistsException extends RuntimeException {
    public ResetCodeAlreadyExistsException(String message) {
        super(message);
    }
}
