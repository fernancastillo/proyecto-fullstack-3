package com.bffservice.client;

import com.bffservice.dto.PatientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "PATIENT-SERVICE")
public interface PatientClient {

    @GetMapping("/patients")
    List<PatientDTO> getAllPatients();

    @GetMapping("/patients/{id}")
    PatientDTO getPatientById(@PathVariable("id") Long id);

    @GetMapping("/patients/rut/{rut}")
    PatientDTO getPatientByRut(@PathVariable("rut") String rut);

    @PostMapping("/patients")
    PatientDTO createPatient(@RequestBody PatientDTO patient);

    @PutMapping("/patients/{id}")
    PatientDTO updatePatient(@PathVariable("id") Long id, @RequestBody PatientDTO patient);

    @DeleteMapping("/patients/{id}")
    void deletePatient(@PathVariable("id") Long id);
}