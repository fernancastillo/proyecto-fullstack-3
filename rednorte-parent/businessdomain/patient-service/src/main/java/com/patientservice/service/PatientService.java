package com.patientservice.service;

import com.patientservice.entity.Patient;
import com.patientservice.exception.ResourceNotFoundException;
import com.patientservice.repository.PatientRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    public Optional<Patient> getPatientByRut(String rut) {
        return patientRepository.findByRut(rut);
    }

    public Patient createPatient(@NonNull Patient patient) {
        if (patientRepository.existsByRut(patient.getRut())) {
            throw new IllegalArgumentException("Ya existe un paciente con el RUT: " + patient.getRut());
        }
        if (patientRepository.existsByEmail(patient.getEmail())) {
            throw new IllegalArgumentException("Ya existe un paciente con el email: " + patient.getEmail());
        }
        return patientRepository.save(patient);
    }

    public Patient updatePatient(Long id, @NonNull Patient patientDetails) {
        return patientRepository.findById(id).map(p -> {
            p.setName(patientDetails.getName());
            p.setLastname(patientDetails.getLastname());
            p.setEmail(patientDetails.getEmail());
            p.setPhone(patientDetails.getPhone());
            p.setRegion(patientDetails.getRegion());
            p.setComuna(patientDetails.getComuna());
            p.setAddress(patientDetails.getAddress());
            return patientRepository.save(p);
        }).orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con el ID: " + id));
    }

    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paciente no encontrado con el ID: " + id);
        }
        patientRepository.deleteById(id);
    }
}