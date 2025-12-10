package br.com.hahn.auth.application.execption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UserCanNotChangePasswordException extends RuntimeException {
    public UserCanNotChangePasswordException(String message) {
        super(message);
    }
}
