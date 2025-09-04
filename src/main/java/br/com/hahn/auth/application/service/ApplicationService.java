package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.ApplicationNotFoundException;
import br.com.hahn.auth.application.execption.UserNotOAuthException;
import br.com.hahn.auth.domain.enums.TypeUser;
import br.com.hahn.auth.domain.model.Application;
import br.com.hahn.auth.domain.respository.ApplicationRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.classfile.instruction.LoadInstruction;

@Service
public class ApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final ApplicationRepository applicationRepository;
    private final TokenService tokenService;
    private final UserService userService;

    public ApplicationService(ApplicationRepository applicationRepository, TokenService tokenService, UserService userService) {
        this.applicationRepository = applicationRepository;
        this.tokenService = tokenService;
        this.userService = userService;
    }

    public Application findById(Long id){
        logger.info("ApplicationService: Find application by ID");
        return applicationRepository.findById(id).orElseThrow(()
                -> new ApplicationNotFoundException("Application not found"));
    }

    public void bindApplicationToOAuthUser(Long applicationId, String authorizationHeader){

        Application application = findById(applicationId);
        var decodeJWT = tokenService.decoteToken(authorizationHeader.replace("Bearer ", ""));

        String typeUser = decodeJWT.getClaim("type_user").asString();
        String userId = decodeJWT.getClaim("user_id").asString();
        verifyIfIsOAuthUser(typeUser);

        userService.addApplicationToUser(userId, application);

    }

    public void verifyIfIsOAuthUser(String userType){
        if(!TypeUser.OAUTH_USER.toString().equals(userType)){
            throw new UserNotOAuthException("User is not on OAuth user");
        }
    }
}
