package com.simats.neoomfs.repository;

import com.simats.neoomfs.entity.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {
    Optional<MedicalHistory> findByPatientId(Long patientId);
    boolean existsByPatientId(Long patientId);
}
