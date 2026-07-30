package com.simats.neoomfs.repository;

import com.simats.neoomfs.entity.Medications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationsRepository extends JpaRepository<Medications, Long> {
    List<Medications> findByPatientId(Long patientId);
    void deleteByPatientId(Long patientId);
}
