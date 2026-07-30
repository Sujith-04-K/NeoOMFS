package com.simats.neoomfs.repository;

import com.simats.neoomfs.entity.DentalExamination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DentalExaminationRepository extends JpaRepository<DentalExamination, Long> {
    Optional<DentalExamination> findByPatientId(Long patientId);
    boolean existsByPatientId(Long patientId);
}
