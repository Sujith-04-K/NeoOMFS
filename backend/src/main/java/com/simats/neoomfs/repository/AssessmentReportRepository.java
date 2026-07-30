package com.simats.neoomfs.repository;

import com.simats.neoomfs.entity.AssessmentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssessmentReportRepository extends JpaRepository<AssessmentReport, Long> {

    Page<AssessmentReport> findByPatientId(Long patientId, Pageable pageable);

    List<AssessmentReport> findByPatientIdOrderByReportGeneratedAtDesc(Long patientId);

    List<AssessmentReport> findByReportGeneratedAtBefore(LocalDateTime cutoff);

}