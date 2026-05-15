package com.bffservice.client;

import com.bffservice.client.fallback.RequestClientFallback;
import com.bffservice.dto.RequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "REQUEST-SERVICE", fallback = RequestClientFallback.class)
public interface RequestClient {

    @GetMapping("/requests")
    List<RequestDTO> getAllRequests();

    @GetMapping("/requests/{id}")
    RequestDTO getRequestById(@PathVariable("id") Long id);

    @GetMapping("/requests/user/{userId}")
    List<RequestDTO> getRequestsByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/requests/medico/{medicoId}")
    List<RequestDTO> getRequestsByMedicoId(@PathVariable("medicoId") Long medicoId);

    @GetMapping("/requests/especialidad/{especialidad}")
    List<RequestDTO> getRequestsByEspecialidad(@PathVariable("especialidad") String especialidad);

    @GetMapping("/requests/estado/{estado}")
    List<RequestDTO> getRequestsByEstado(@PathVariable("estado") String estado);

    @PostMapping("/requests")
    RequestDTO createRequest(@RequestBody RequestDTO request);

    @PutMapping("/requests/{id}")
    RequestDTO updateRequest(@PathVariable("id") Long id, @RequestBody RequestDTO request);

    @DeleteMapping("/requests/{id}")
    void deleteRequest(@PathVariable("id") Long id);
}