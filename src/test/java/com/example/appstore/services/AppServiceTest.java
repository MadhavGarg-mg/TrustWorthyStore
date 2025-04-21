package com.example.appstore.services;

import com.example.appstore.models.AppEntry;
import com.example.appstore.repository.AppEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppServiceTest {

    @Mock
    private AppEntryRepository repo;

    @InjectMocks
    private AppService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private MultipartFile makeFile(String name, byte[] data) throws IOException {
        MultipartFile f = mock(MultipartFile.class);
        when(f.getOriginalFilename()).thenReturn(name);
        when(f.getBytes()).thenReturn(data);
        return f;
    }

    @Test
    void saveAppAndMetadata_savesAndReturnsEntity() throws IOException {
        MultipartFile appFile      = makeFile("app.bin", new byte[]{1,2,3});
        MultipartFile metadataFile = makeFile("meta.json", "{\"k\":1}".getBytes());

        AppEntry saved = new AppEntry();
        saved.setId(42L);
        when(repo.save(any(AppEntry.class)))
                .thenAnswer(invocation -> {
                    AppEntry in = invocation.getArgument(0);
                    // simulate DB assigning date and id
                    in.setUploadDate(LocalDateTime.of(2025,4,20,12,0));
                    in.setId(99L);
                    return in;
                });

        AppEntry result = service.saveAppAndMetadata("MyApp", appFile, metadataFile);

        assertEquals(99L, result.getId());
        assertEquals("MyApp", result.getAppName());
        assertArrayEquals(new byte[]{1,2,3}, result.getAppFile());
        assertArrayEquals("{\"k\":1}".getBytes(), result.getMetadataFile());
        assertNotNull(result.getUploadDate());
        verify(repo).save(any(AppEntry.class));
    }

    @Test
    void getAllApps_returnsListFromRepo() {
        List<AppEntry> list = Arrays.asList(new AppEntry(), new AppEntry());
        when(repo.findAll()).thenReturn(list);

        List<AppEntry> result = service.getAllApps();
        assertSame(list, result);
    }

    @Test
    void getAppEntry_found() {
        AppEntry e = new AppEntry();
        when(repo.findById(5L)).thenReturn(Optional.of(e));
        assertSame(e, service.getAppEntry(5L));
    }

    @Test
    void getAppEntry_notFound() {
        when(repo.findById(123L)).thenReturn(Optional.empty());
        assertNull(service.getAppEntry(123L));
    }
}
