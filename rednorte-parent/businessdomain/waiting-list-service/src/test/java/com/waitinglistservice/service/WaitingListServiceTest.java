package com.waitinglistservice.service;

import com.waitinglistservice.entity.WaitingList;
import com.waitinglistservice.exception.ResourceNotFoundException;
import com.waitinglistservice.repository.WaitingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WaitingListServiceTest {

    @Mock
    private WaitingListRepository repository;

    @InjectMocks
    private WaitingListService waitingListService;

    private WaitingList mockEntry;

    @BeforeEach
    void setUp() {
        mockEntry = new WaitingList(
            1L, 1L, 2L, "Traumatología",
            "ALTA", "EN_ESPERA", LocalDateTime.now()
        );
    }

    @Test
    void testGetAll() {
        when(repository.findAll()).thenReturn(Arrays.asList(mockEntry));

        List<WaitingList> result = waitingListService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Traumatología", result.get(0).getSpecialty());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGetById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(mockEntry));

        Optional<WaitingList> result = waitingListService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals("EN_ESPERA", result.get().getStatus());
    }

    @Test
    void testGetById_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<WaitingList> result = waitingListService.getById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void testCreate() {
        when(repository.save(any(WaitingList.class))).thenReturn(mockEntry);

        WaitingList created = waitingListService.create(mockEntry);

        assertNotNull(created);
        assertEquals("ALTA", created.getPriority());
        verify(repository, times(1)).save(mockEntry);
    }

    @Test
    void testDelete_Success() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> waitingListService.delete(1L));
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_NotFound_ThrowsException() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
            waitingListService.delete(99L)
        );
        verify(repository, never()).deleteById(any());
    }

    @Test
    void testGetByStatus() {
        when(repository.findByStatus("EN_ESPERA")).thenReturn(Arrays.asList(mockEntry));

        List<WaitingList> result = waitingListService.getByStatus("EN_ESPERA");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EN_ESPERA", result.get(0).getStatus());
    }

    @Test
    void testGetByPriority() {
        when(repository.findByPriority("ALTA")).thenReturn(Arrays.asList(mockEntry));

        List<WaitingList> result = waitingListService.getByPriority("ALTA");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ALTA", result.get(0).getPriority());
    }
}