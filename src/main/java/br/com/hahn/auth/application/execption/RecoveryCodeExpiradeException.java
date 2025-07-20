package br.com.hahn.auth.application.execption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class RecoveryCodeExpiradeException extends RuntimeException {
    public RecoveryCodeExpiradeException(String message) {
        super(message);
    }
}
