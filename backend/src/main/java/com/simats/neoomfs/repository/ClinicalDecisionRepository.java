package com.simats.neoomfs.repository;

import com.simats.neoomfs.entity.ClinicalDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicalDecisionRepository extends JpaRepository<ClinicalDecision, Long> {

    Optional<ClinicalDecision> findByPatientId(Long patientId);

    boolean existsByPatientId(Long patientId);

    long countByFitnessDecision(ClinicalDecision.FitnessDecision fitnessDecision);

    long countByRiskLevel(ClinicalDecision.RiskLevel riskLevel);
}