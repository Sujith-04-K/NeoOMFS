package com.simats.neoomfs.repository;

import com.simats.neoomfs.entity.PatientVitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientVitalsRepository extends JpaRepository<PatientVitals, Long> {
    Optional<PatientVitals> findByPatientId(Long patientId);
    boolean existsByPatientId(Long patientId);
}
