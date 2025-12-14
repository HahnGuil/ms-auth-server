package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.ApplicationNotFoundException;
import br.com.hahn.auth.domain.model.Application;
import br.com.hahn.auth.domain.respository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void shouldFindApplicationByIdWhenApplicationExists() {
        Long id = 1L;
        Application application = new Application();
        application.setId(id);

        when(applicationRepository.findById(id)).thenReturn(Optional.of(application));

        Application result = applicationService.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(applicationRepository).findById(id);
    }

    @Test
    void shouldThrowApplicationNotFoundExceptionWhenApplicationDoesNotExist() {
        Long id = 1L;

        when(applicationRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> applicationService.findById(id));
        verify(applicationRepository).findById(id);
    }
}