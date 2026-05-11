package com.bffservice.client;

import com.bffservice.dto.RequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "REQUEST-SERVICE")
public interface RequestClient {

    @GetMapping("/requests")
    List<RequestDTO> getAllRequests();

    @GetMapping("/requests/{id}")
    RequestDTO getRequestById(@PathVariable("id") Long id);

    @GetMapping("/requests/rut/{rut}")
    List<RequestDTO> getRequestsByRut(@PathVariable("rut") String rut);

    @PostMapping("/requests")
    RequestDTO createRequest(@RequestBody RequestDTO request);

    @PutMapping("/requests/{id}")
    RequestDTO updateRequest(@PathVariable("id") Long id, @RequestBody RequestDTO request);

    @DeleteMapping("/requests/{id}")
    void deleteRequest(@PathVariable("id") Long id);
}