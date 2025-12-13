package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.ApplicationNotFoundException;
import br.com.hahn.auth.domain.model.Application;
import br.com.hahn.auth.domain.respository.ApplicationRepository;
import br.com.hahn.auth.util.DateTimeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    /**
     * Finds an application by its ID.
     * <p>
     * This method logs an informational message when starting the lookup and an error
     * message if the application is not found. If the application cannot be found,
     * an {@link ApplicationNotFoundException} is thrown.
     *
     * @author HahnGuil
     * @param id the ID of the application to find
     * @return the Application matching the provided ID
     * @throws ApplicationNotFoundException if the application is not found
     */
    public Application findById(Long id) {
        log.info("ApplicationService: Find application: {} at: {}", id, Instant.now());
        return applicationRepository.findById(id).orElseThrow(() -> {
            log.error("ApplicationService: Application not found for id: {}. Throw the ApplicationNotFoundException at: {}", id, DateTimeConverter.formatInstantNow());
            return new ApplicationNotFoundException("Application not found");
        });
    }
}
