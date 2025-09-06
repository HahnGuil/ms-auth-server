package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.ApplicationNotFoundException;
import br.com.hahn.auth.domain.model.Application;
import br.com.hahn.auth.domain.respository.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }


    public Application findById(Long id){
        logger.info("ApplicationService: Find application by ID");
        return applicationRepository.findById(id).orElseThrow(()
                -> new ApplicationNotFoundException("Application not found"));
    }

}
