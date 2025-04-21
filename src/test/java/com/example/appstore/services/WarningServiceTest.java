package com.example.appstore.services;

import com.example.appstore.models.User;
import com.example.appstore.models.Warning;
import com.example.appstore.repository.UserRepository;
import com.example.appstore.repository.WarningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WarningServiceTest {

    @Mock
    private WarningRepository warnRepo;

    @Mock
    private UserRepository userRepo;

    private WarningService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new WarningService(warnRepo, userRepo);
    }

    @Test
    void issueWarning_underThreshold_doesNotSuspend() {
        User dev = new User();
        dev.setSuspended(false);
        when(warnRepo.countByDeveloper(dev)).thenReturn(1L);

        long count = service.issueWarning(dev, "reason");

        verify(warnRepo).save(any(Warning.class));
        verify(userRepo, never()).save(any());
        assertEquals(1L, count);
        assertFalse(dev.isSuspended());
    }

    @Test
    void issueWarning_atThreshold_suspendsUser() {
        User dev = new User();
        dev.setSuspended(false);
        when(warnRepo.countByDeveloper(dev)).thenReturn(3L);

        long count = service.issueWarning(dev, "reason2");

        verify(warnRepo).save(any(Warning.class));
        verify(userRepo).save(dev);
        assertTrue(dev.isSuspended());
        assertEquals(3L, count);
    }

    @Test
    void clearWarnings_deletesAllForDev() {
        User dev = new User();

        service.clearWarnings(dev);

        verify(warnRepo).deleteByDeveloper(dev);
    }
}
