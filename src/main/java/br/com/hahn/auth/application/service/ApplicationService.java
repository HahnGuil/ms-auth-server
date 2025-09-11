package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.ApplicationNotFoundException;
import br.com.hahn.auth.domain.model.Application;
import br.com.hahn.auth.domain.respository.ApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }


    public Application findById(Long id){
        log.info("ApplicationService: Find application by ID");
        return applicationRepository.findById(id).orElseThrow(()
                -> new ApplicationNotFoundException("Application not found"));
    }

}
