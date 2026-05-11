package com.bffservice.client;

import com.bffservice.dto.WaitingListDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "WAITING-LIST-SERVICE")
public interface WaitingListClient {

    @GetMapping("/waiting-list")
    List<WaitingListDTO> getAll();

    @GetMapping("/waiting-list/{id}")
    WaitingListDTO getById(@PathVariable("id") Long id);

    @GetMapping("/waiting-list/patient/{patientId}")
    List<WaitingListDTO> getByPatientId(@PathVariable("patientId") Long patientId);

    @PostMapping("/waiting-list")
    WaitingListDTO create(@RequestBody WaitingListDTO waitingList);

    @PutMapping("/waiting-list/{id}")
    WaitingListDTO update(@PathVariable("id") Long id, @RequestBody WaitingListDTO waitingList);

    @DeleteMapping("/waiting-list/{id}")
    void delete(@PathVariable("id") Long id);
}