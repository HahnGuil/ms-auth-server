package br.com.hahn.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MsAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsAuthServerApplication.class, args);
    }

}
