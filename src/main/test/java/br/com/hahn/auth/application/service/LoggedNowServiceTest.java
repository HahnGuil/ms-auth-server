package br.com.hahn.auth.application.service;

import br.com.hahn.auth.domain.model.LoggedNow;
import br.com.hahn.auth.domain.respository.LoggedNowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LoggedNowServiceTest {

    @Mock
    private LoggedNowRepository loggedNowRepository;

    @InjectMocks
    private LoggedNowService loggedNowService;

    @Test
    void shouldReturnLoggedNowListWhenUserIdExists() {
        UUID userId = UUID.randomUUID();
        LoggedNow loggedNow = new LoggedNow();
        loggedNow.setUserId(userId);

        when(loggedNowRepository.findByUserId(userId)).thenReturn(List.of(loggedNow));

        List<LoggedNow> result = loggedNowService.findByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(loggedNowRepository, times(1)).findByUserId(userId);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoLoggedSessions() {
        UUID userId = UUID.randomUUID();

        when(loggedNowRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<LoggedNow> result = loggedNowService.findByUserId(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(loggedNowRepository, times(1)).findByUserId(userId);
    }

    @Test
    void shouldSaveLoggedNowSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID tokenLogId = UUID.randomUUID();
        LocalDateTime dateLogin = LocalDateTime.now();

        loggedNowService.save(userId, tokenLogId, dateLogin);

        ArgumentCaptor<LoggedNow> captor = ArgumentCaptor.forClass(LoggedNow.class);
        verify(loggedNowRepository, times(1)).save(captor.capture());

        LoggedNow savedLoggedNow = captor.getValue();
        assertEquals(userId, savedLoggedNow.getUserId());
        assertEquals(tokenLogId, savedLoggedNow.getTokenLogId());
        assertEquals(dateLogin, savedLoggedNow.getDateLogin());
        assertFalse(savedLoggedNow.isUseRefresh());
        assertNull(savedLoggedNow.getDateRefresh());
    }

    @Test
    void shouldDeleteLoggedNowByUserId() {
        UUID userId = UUID.randomUUID();

        loggedNowService.deleteByUserId(userId);

        verify(loggedNowRepository, times(1)).deleteByUserId(userId);
    }

    @Test
    void shouldReturnMultipleLoggedSessionsForUser() {
        UUID userId = UUID.randomUUID();
        LoggedNow loggedNow1 = new LoggedNow();
        loggedNow1.setUserId(userId);
        LoggedNow loggedNow2 = new LoggedNow();
        loggedNow2.setUserId(userId);

        when(loggedNowRepository.findByUserId(userId)).thenReturn(List.of(loggedNow1, loggedNow2));

        List<LoggedNow> result = loggedNowService.findByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(loggedNowRepository, times(1)).findByUserId(userId);
    }

    @Test
    void shouldSaveLoggedNowWithCorrectDefaultValues() {
        UUID userId = UUID.randomUUID();
        UUID tokenLogId = UUID.randomUUID();
        LocalDateTime dateLogin = LocalDateTime.of(2023, 12, 14, 10, 30);

        loggedNowService.save(userId, tokenLogId, dateLogin);

        ArgumentCaptor<LoggedNow> captor = ArgumentCaptor.forClass(LoggedNow.class);
        verify(loggedNowRepository, times(1)).save(captor.capture());

        LoggedNow savedLoggedNow = captor.getValue();
        assertFalse(savedLoggedNow.isUseRefresh());
        assertNull(savedLoggedNow.getDateRefresh());
    }
}