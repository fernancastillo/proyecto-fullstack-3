package com.requestservice.service;

import com.requestservice.entity.Request;
import com.requestservice.exception.ResourceNotFoundException;
import com.requestservice.repository.RequestRepository;
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
public class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private RequestService requestService;

    private Request mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = new Request(
            1L, 1L, 2L, "Cardiología",
            "Dolor en el pecho", "PENDIENTE",
            LocalDateTime.now(), null
        );
    }

    @Test
    void testGetAllRequests() {
        when(requestRepository.findAll()).thenReturn(Arrays.asList(mockRequest));

        List<Request> result = requestService.getAllRequests();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cardiología", result.get(0).getEspecialidad());
        verify(requestRepository, times(1)).findAll();
    }

    @Test
    void testGetRequestById_Success() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(mockRequest));

        Optional<Request> result = requestService.getRequestById(1L);

        assertTrue(result.isPresent());
        assertEquals("PENDIENTE", result.get().getEstado());
        verify(requestRepository, times(1)).findById(1L);
    }

    @Test
    void testGetRequestById_NotFound() {
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Request> result = requestService.getRequestById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void testCreateRequest() {
        when(requestRepository.save(any(Request.class))).thenReturn(mockRequest);

        Request created = requestService.createRequest(mockRequest);

        assertNotNull(created);
        assertEquals(1L, created.getUserId());
        assertEquals("Cardiología", created.getEspecialidad());
        verify(requestRepository, times(1)).save(mockRequest);
    }

    @Test
    void testUpdateRequest_NotFound_ThrowsException() {
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            requestService.updateRequest(99L, mockRequest)
        );
    }

    @Test
    void testDeleteRequest_Success() {
        when(requestRepository.existsById(1L)).thenReturn(true);
        doNothing().when(requestRepository).deleteById(1L);

        assertDoesNotThrow(() -> requestService.deleteRequest(1L));
        verify(requestRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteRequest_NotFound_ThrowsException() {
        when(requestRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
            requestService.deleteRequest(99L)
        );
        verify(requestRepository, never()).deleteById(any());
    }

    @Test
    void testGetRequestsByEstado() {
        when(requestRepository.findByEstado("PENDIENTE")).thenReturn(Arrays.asList(mockRequest));

        List<Request> result = requestService.getRequestsByEstado("PENDIENTE");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PENDIENTE", result.get(0).getEstado());
    }

    @Test
    void testGetRequestsByUserId() {
        when(requestRepository.findByUserId(1L)).thenReturn(Arrays.asList(mockRequest));

        List<Request> result = requestService.getRequestsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
    }
}