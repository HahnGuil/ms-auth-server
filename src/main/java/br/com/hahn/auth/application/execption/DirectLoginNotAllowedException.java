package br.com.hahn.auth.application.execption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class DirectLoginNotAllowedException extends RuntimeException {
    public DirectLoginNotAllowedException(String message) {
        super(message);
    }
}
