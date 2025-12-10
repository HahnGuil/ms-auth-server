package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.ApplicationNotFoundException;
import br.com.hahn.auth.domain.model.Application;
import br.com.hahn.auth.domain.respository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public Application findById(Long id) {
        log.info("ApplicationService: Find application: {} at: {}", id, Instant.now());
        return applicationRepository.findById(id).orElseThrow(() -> {
            log.error("ApplicationService: Application not found for id: {}. Throw the ApplicationNotFoundException at: {}", id, Instant.now());
            return new ApplicationNotFoundException("Application not found");
        });
    }
}
