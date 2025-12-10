package br.com.hahn.auth.application.execption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundResetPasswordRequestForUser extends RuntimeException {
    public NotFoundResetPasswordRequestForUser(String message) {
        super(message);
    }
}
