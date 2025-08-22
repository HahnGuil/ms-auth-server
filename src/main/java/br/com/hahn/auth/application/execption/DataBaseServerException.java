package br.com.hahn.auth.application.execption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DataBaseServerException extends RuntimeException {
    public DataBaseServerException(String message) {
        super(message);
    }
}
