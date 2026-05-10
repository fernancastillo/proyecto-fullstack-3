package com.waitinglistservice.repository;

import com.waitinglistservice.entity.WaitingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaitingListRepository extends JpaRepository<WaitingList, Long> {
    
    List<WaitingList> findByPatientId(Long patientId);
    List<WaitingList> findBySpecialty(String specialty);
    List<WaitingList> findByStatus(String status);
}
