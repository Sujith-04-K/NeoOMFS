package com.simats.neoomfs.repository;

import com.simats.neoomfs.entity.LaboratoryInvestigations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LaboratoryInvestigationsRepository extends JpaRepository<LaboratoryInvestigations, Long> {
    Optional<LaboratoryInvestigations> findByPatientId(Long patientId);
    boolean existsByPatientId(Long patientId);
}
