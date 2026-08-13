package com.simats.neoomfs.repository;

import com.simats.neoomfs.entity.Patient;
import com.simats.neoomfs.entity.ClinicalDecision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByMrnAndDeletedFalse(String mrn);

    Optional<Patient> findByIdAndDeletedFalse(Long id);

    @Query(value = "SELECT p FROM Patient p LEFT JOIN FETCH p.createdBy LEFT JOIN FETCH p.referringDoctor WHERE p.deleted = false AND " +
            "(:search IS NULL OR :search = '' OR " +
            " LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            " LOWER(p.mrn) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            " LOWER(p.createdBy.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            " LOWER(p.createdBy.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            " LOWER(p.createdBy.licenseNumber) LIKE LOWER(CONCAT('%', :search, '%'))" +
            ") AND " +
            "(:status IS NULL OR p.assessmentStatus = :status) AND " +
            "(:doctorId IS NULL OR " +
            "   (:doctorId > 0 AND p.createdBy.id = :doctorId) OR " +
            "   (:doctorId < 0 AND p.referringDoctor.id = -:doctorId)" +
            ")",
            countQuery = "SELECT COUNT(p) FROM Patient p WHERE p.deleted = false AND " +
            "(:search IS NULL OR :search = '' OR " +
            " LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            " LOWER(p.mrn) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            " LOWER(p.createdBy.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            " LOWER(p.createdBy.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            " LOWER(p.createdBy.licenseNumber) LIKE LOWER(CONCAT('%', :search, '%'))" +
            ") AND " +
            "(:status IS NULL OR p.assessmentStatus = :status) AND " +
            "(:doctorId IS NULL OR " +
            "   (:doctorId > 0 AND p.createdBy.id = :doctorId) OR " +
            "   (:doctorId < 0 AND p.referringDoctor.id = -:doctorId)" +
            ")")
    Page<Patient> searchPatients(
            @Param("search") String search,
            @Param("status") Patient.AssessmentStatus status,
            @Param("doctorId") Long doctorId,
            Pageable pageable
    );

    @Query(value = "SELECT DISTINCT p FROM Patient p " +
            "LEFT JOIN FETCH p.createdBy d " +
            "LEFT JOIN FETCH p.referringDoctor rd " +
            "LEFT JOIN ClinicalDecision cd ON cd.patient = p " +
            "WHERE p.deleted = false AND " +
            "(:mrn IS NULL OR :mrn = '' OR LOWER(p.mrn) LIKE LOWER(CONCAT('%', :mrn, '%'))) AND " +
            "(:name IS NULL OR :name = '' OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:phone IS NULL OR :phone = '' OR p.phoneNumber LIKE CONCAT('%', :phone, '%')) AND " +
            "(:doctor IS NULL OR :doctor = '' OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', :doctor, '%')) OR LOWER(rd.fullName) LIKE LOWER(CONCAT('%', :doctor, '%'))) AND " +
            "(:status IS NULL OR p.assessmentStatus = :status) AND " +
            "(:risk IS NULL OR cd.riskLevel = :risk) AND " +
            "(:gender IS NULL OR :gender = '' OR LOWER(p.gender) = LOWER(:gender)) AND " +
            "(:age IS NULL OR p.age = :age)",
            countQuery = "SELECT COUNT(DISTINCT p) FROM Patient p " +
            "LEFT JOIN p.referringDoctor rd " +
            "LEFT JOIN ClinicalDecision cd ON cd.patient = p " +
            "WHERE p.deleted = false AND " +
            "(:mrn IS NULL OR :mrn = '' OR LOWER(p.mrn) LIKE LOWER(CONCAT('%', :mrn, '%'))) AND " +
            "(:name IS NULL OR :name = '' OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:phone IS NULL OR :phone = '' OR p.phoneNumber LIKE CONCAT('%', :phone, '%')) AND " +
            "(:doctor IS NULL OR :doctor = '' OR LOWER(p.createdBy.fullName) LIKE LOWER(CONCAT('%', :doctor, '%')) OR LOWER(rd.fullName) LIKE LOWER(CONCAT('%', :doctor, '%'))) AND " +
            "(:status IS NULL OR p.assessmentStatus = :status) AND " +
            "(:risk IS NULL OR cd.riskLevel = :risk) AND " +
            "(:gender IS NULL OR :gender = '' OR LOWER(p.gender) = LOWER(:gender)) AND " +
            "(:age IS NULL OR p.age = :age)")
    Page<Patient> advancedSearch(
            @Param("mrn") String mrn,
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("doctor") String doctor,
            @Param("status") Patient.AssessmentStatus status,
            @Param("risk") ClinicalDecision.RiskLevel risk,
            @Param("gender") String gender,
            @Param("age") Integer age,
            Pageable pageable
    );

    boolean existsByMrn(String mrn);

    long countByDeletedFalse();

    long countByCreatedAtAfter(LocalDateTime dateTime);

    long countByGenderIgnoreCaseAndDeletedFalse(String gender);

    @Query("SELECT COALESCE(AVG(p.age), 0.0) FROM Patient p WHERE p.deleted = false")
    Double getAverageAge();

    java.util.List<Patient> findByDeletedFalse();
}
