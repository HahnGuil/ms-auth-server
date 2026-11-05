package br.com.hahn.auth.application.controller;

import br.com.hahn.auth.application.execption.InvalidFormatTypeException;
import br.com.hahn.auth.application.service.ApplicationService;
import br.com.hahn.auth.application.service.UserService;
import br.com.hahn.auth.domain.model.ApplicationCreateRequest;
import br.com.hahn.auth.domain.model.ApplicationResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationControllerTest {
    private final ApplicationService applicationService = mock(ApplicationService.class);
    private final UserService userService = mock(UserService.class);
    private final ApplicationController controller = new ApplicationController(applicationService, userService);
    private ApplicationCreateRequest request(String email) {
        ApplicationCreateRequest request = new ApplicationCreateRequest(); request.setNameApplication("Store"); request.setApplicationEmail(email); return request;
    }
    @Test void createsApplicationOnlyAfterAdminValidation() {
        Jwt jwt = mock(Jwt.class); var spy = Mockito.spy(controller); doReturn(jwt).when(spy).extractJwtFromContext();
        var request = request("security@example.com"); var response = new ApplicationResponse(); response.setApplicationPublicId(UUID.randomUUID());
        when(applicationService.createApplication(request)).thenReturn(response);
        var result = spy.postCreateApplication(request);
        assertEquals(HttpStatus.CREATED, result.getStatusCode()); assertSame(response, result.getBody());
        verify(userService).validateAdminUser(jwt); verify(applicationService).createApplication(request);
    }
    @Test void rejectsInvalidApplicationEmailBeforeCreatingApplication() {
        Jwt jwt = mock(Jwt.class); var spy = Mockito.spy(controller); doReturn(jwt).when(spy).extractJwtFromContext();
        assertThrows(InvalidFormatTypeException.class, () -> spy.postCreateApplication(request("invalid")));
        verify(userService).validateAdminUser(jwt); verifyNoInteractions(applicationService);
    }
}
