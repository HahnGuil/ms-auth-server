package br.com.hahn.auth.application.service;

import br.com.hahn.auth.application.execption.ApplicationNotFoundException;
import br.com.hahn.auth.application.execption.InvalidFormatTypeException;
import br.com.hahn.auth.application.execption.ResourceAlreadyExistException;
import br.com.hahn.auth.domain.enums.ScopeToken;
import br.com.hahn.auth.domain.model.*;
import br.com.hahn.auth.domain.respository.ApplicationRepository;
import br.com.hahn.auth.domain.respository.PasswordPolicyRepository;
import br.com.hahn.auth.infrastructure.security.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {
    @Mock ApplicationRepository applicationRepository;
    @Mock PasswordPolicyRepository passwordPolicyRepository;
    @Mock TokenLogService tokenLogService;
    @Mock TokenService tokenService;
    @InjectMocks ApplicationService applicationService;

    private ApplicationCreateRequest request(Boolean define, Boolean useDefault, Integer months) {
        ApplicationCreateRequest r = new ApplicationCreateRequest();
        r.setNameApplication(" Store "); r.setApplicationEmail("security@example.com"); r.setApplicationDomain("store.example.com");
        r.setResendApiKey(" key "); r.setDefinePasswordPolicy(define); r.setUseDefaultPasswordPolicy(useDefault); r.setPasswordExpirationMonths(months); return r;
    }

    @Test void shouldFindApplicationByIdWhenApplicationExists() {
        Application app = new Application(); app.setId(1L);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        assertSame(app, applicationService.findById(1L)); verify(applicationRepository).findById(1L);
    }
    @Test void shouldThrowApplicationNotFoundExceptionWhenApplicationDoesNotExist() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ApplicationNotFoundException.class, () -> applicationService.findById(1L));
    }

    @Test void storesDisabledPolicyWhenPolicyNotDefined() {
        verifyCreatedPolicy(request(false, false, null), false, false, null);
    }
    @Test void defaultOptionDoesNotEnablePolicyWhenPolicyWasNotDefined() {
        verifyCreatedPolicy(request(false, true, 12), false, false, null);
    }
    @Test void storesDefaultNinetyDayPolicy() {
        verifyCreatedPolicy(request(true, true, null), true, true, null);
    }
    @Test void storesCustomPolicyInMonths() {
        verifyCreatedPolicy(request(true, false, 4), true, false, 4);
    }
    private void verifyCreatedPolicy(ApplicationCreateRequest request, boolean enabled, boolean defaultPolicy, Integer months) {
        UUID publicId = UUID.randomUUID();
        when(applicationRepository.existsByNameApplicationIgnoreCaseOrApplicationDomainIgnoreCaseOrApplicationEmailIgnoreCase("Store", "store.example.com", "security@example.com")).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> { Application app = inv.getArgument(0); app.setId(10L); app.setPublicId(publicId); return app; });
        ApplicationResponse response = applicationService.createApplication(request);
        assertEquals(publicId, response.getApplicationPublicId()); assertEquals("Store", response.getNameApplication());
        ArgumentCaptor<PasswordPolicy> policy = ArgumentCaptor.forClass(PasswordPolicy.class);
        verify(passwordPolicyRepository).save(policy.capture());
        assertEquals(enabled, policy.getValue().isExpirationEnabled()); assertEquals(defaultPolicy, policy.getValue().isDefaultPolicy());
        assertEquals(months, policy.getValue().getExpirationMonths()); assertEquals(10L, policy.getValue().getApplication().getId());
    }

    @Test void rejectsMissingCustomExpirationMonths() {
        assertThrows(InvalidFormatTypeException.class, () -> applicationService.createApplication(request(true, false, null)));
        assertThrows(InvalidFormatTypeException.class, () -> applicationService.createApplication(request(true, false, 0)));
        verifyNoInteractions(applicationRepository, passwordPolicyRepository);
    }
    @Test void rejectsMissingRequiredApplicationData() {
        var invalid = request(false, false, null); invalid.setApplicationEmail(" ");
        assertThrows(InvalidFormatTypeException.class, () -> applicationService.createApplication(invalid));
        verifyNoInteractions(applicationRepository, passwordPolicyRepository);
    }
    @Test void rejectsDuplicateNameDomainOrEmail() {
        when(applicationRepository.existsByNameApplicationIgnoreCaseOrApplicationDomainIgnoreCaseOrApplicationEmailIgnoreCase(anyString(), anyString(), anyString())).thenReturn(true);
        assertThrows(ResourceAlreadyExistException.class, () -> applicationService.createApplication(request(false, false, null)));
        verify(applicationRepository, never()).save(any()); verifyNoInteractions(passwordPolicyRepository);
    }

    @Test void registersApplicationTokens() {
        UUID id = UUID.randomUUID(); Application app = new Application(); app.setPublicId(id); app.setNameApplication("A");
        when(applicationRepository.findApplicationByPublicId(id)).thenReturn(Optional.of(app));
        TokenLog tokenLog = new TokenLog();
        when(tokenLogService.saveApplicationTokenLog(eq(app), any(), any())).thenReturn(tokenLog);
        when(tokenService.generateApplicationToken(app, tokenLog)).thenReturn("access");
        when(tokenService.generateApplicationRefreshToken(app, tokenLog)).thenReturn("refresh");
        var response = applicationService.registerApplication(id);
        assertEquals("access", response.getApplicationToken()); assertEquals("refresh", response.getRefreshApplicationToken());
    }
}
