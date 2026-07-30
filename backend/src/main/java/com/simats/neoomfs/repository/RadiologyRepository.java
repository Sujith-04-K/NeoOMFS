package com.simats.neoomfs.repository;

import com.simats.neoomfs.entity.Radiology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RadiologyRepository extends JpaRepository<Radiology, Long> {
    Optional<Radiology> findByPatientId(Long patientId);
    boolean existsByPatientId(Long patientId);
}
