package br.com.hahn.auth.application.execption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class UserNotOAuthException extends RuntimeException {
    public UserNotOAuthException(String message) {
        super(message);
    }
}
