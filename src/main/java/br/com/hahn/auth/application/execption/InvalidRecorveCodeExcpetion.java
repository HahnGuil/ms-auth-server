package br.com.hahn.auth.application.execption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidRecorveCodeExcpetion extends RuntimeException {
    public InvalidRecorveCodeExcpetion(String message) {
        super(message);
    }
}
