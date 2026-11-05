package br.com.hahn.auth.application.service;

import java.time.ZoneId;
import br.com.hahn.auth.domain.model.LoggedNow;
import br.com.hahn.auth.domain.respository.LoggedNowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggedNowServiceTest {
    @Mock LoggedNowRepository repository;
    @InjectMocks LoggedNowService service;

    @Test
    void findsSessionsOnlyForRequestedApplication() {
        UUID userId = UUID.randomUUID(), appId = UUID.randomUUID();
        var session = new LoggedNow();
        when(repository.findByUserIdAndApplicationPublicId(userId, appId)).thenReturn(List.of(session));
        assertEquals(List.of(session), service.findByUserIdAndApplication(userId, appId));
        verify(repository).findByUserIdAndApplicationPublicId(userId, appId);
        verify(repository, never()).findByUserIdAndApplicationPublicIdIsNull(any());
    }

    @Test
    void findsLegacySessionsWithoutApplication() {
        UUID userId = UUID.randomUUID();
        when(repository.findByUserIdAndApplicationPublicIdIsNull(userId)).thenReturn(List.of());
        assertTrue(service.findByUserIdAndApplication(userId, null).isEmpty());
        verify(repository).findByUserIdAndApplicationPublicIdIsNull(userId);
    }

    @Test
    void savesSessionWithUserApplicationAndToken() {
        UUID userId = UUID.randomUUID(), appId = UUID.randomUUID(), tokenId = UUID.randomUUID();
        LocalDateTime loginTime = LocalDateTime.now(ZoneId.systemDefault());
        service.save(userId, appId, tokenId, loginTime);
        ArgumentCaptor<LoggedNow> captor = ArgumentCaptor.forClass(LoggedNow.class);
        verify(repository).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(appId, saved.getApplicationPublicId());
        assertEquals(tokenId, saved.getTokenLogId());
        assertEquals(loginTime, saved.getDateLogin());
        assertFalse(saved.isUseRefresh());
        assertNull(saved.getDateRefresh());
    }

    @Test
    void deletesSessionsOnlyWithinApplication() {
        UUID userId = UUID.randomUUID(), appId = UUID.randomUUID();
        service.deleteByUserIdAndApplication(userId, appId);
        verify(repository).deleteByUserIdAndApplicationPublicId(userId, appId);
        verify(repository, never()).deleteByUserIdWithoutApplication(any());
    }

    @Test
    void deletesLegacySessionsWhenApplicationIsNull() {
        UUID userId = UUID.randomUUID();
        service.deleteByUserIdAndApplication(userId, null);
        verify(repository).deleteByUserIdWithoutApplication(userId);
    }
}
