package com.simats.neoomfs.service;

import com.simats.neoomfs.dto.request.PatientRequest;
import com.simats.neoomfs.dto.response.PagedResponse;
import com.simats.neoomfs.dto.response.PatientResponse;
import com.simats.neoomfs.dto.response.TimelineEventResponse;

import java.util.List;

public interface PatientService {
    PatientResponse createPatient(PatientRequest request, String doctorEmail);
    PatientResponse getPatient(Long id);
    PatientResponse getPatientByMrn(String mrn);
    PatientResponse updatePatient(Long id, PatientRequest request);
    void deletePatient(Long id);
    PagedResponse<PatientResponse> searchPatients(String search, String status, Long doctorId,
                                                   int page, int size);
    List<TimelineEventResponse> getPatientTimeline(Long patientId);
    
    PagedResponse<PatientResponse> advancedSearch(
            String mrn, String name, String phone, String doctor,
            String status, String risk, String gender, Integer age,
            int page, int size
    );
}
