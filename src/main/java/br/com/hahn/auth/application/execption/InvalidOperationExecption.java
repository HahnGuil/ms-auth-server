package br.com.hahn.auth.application.execption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOperationExecption extends RuntimeException {
    public InvalidOperationExecption(String message) {
        super(message);
    }
}
