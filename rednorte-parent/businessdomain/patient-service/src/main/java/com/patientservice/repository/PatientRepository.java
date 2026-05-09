package com.patientservice.repository;

import com.patientservice.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

Optional<Patient> findByRut(String rut);

Optional<Patient> findByEmail(String email);

boolean existsByRut(String rut);

boolean existsByEmail(String email);

}
