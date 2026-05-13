package com.patientservice.logs.repository;

import com.patientservice.logs.entity.LogRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRequestRepository extends JpaRepository<LogRequest, Long> {
}