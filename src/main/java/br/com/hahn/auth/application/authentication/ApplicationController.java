package br.com.hahn.auth.application.authentication;

import br.com.hahn.auth.application.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/application")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PutMapping("/applicationId/bind")
    public ResponseEntity<?> bindApplicationToOAuthUser(@PathVariable Long applicationId, @RequestHeader("Authorization") String authorizationHeader){



    }





}
