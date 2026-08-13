package com.simats.neoomfs.controller;

import com.simats.neoomfs.dto.request.PatientRequest;
import com.simats.neoomfs.dto.request.ReviewStatusRequest;
import com.simats.neoomfs.dto.response.ApiResponse;
import com.simats.neoomfs.dto.response.PagedResponse;
import com.simats.neoomfs.dto.response.PatientResponse;
import com.simats.neoomfs.dto.response.TimelineEventResponse;
import com.simats.neoomfs.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY','ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<PatientResponse>> createPatient(
            @Valid @RequestBody PatientRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PatientResponse patient = patientService.createPatient(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Patient created successfully", patient));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatient(id)));
    }

    @GetMapping("/mrn/{mrn}")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientByMrn(@PathVariable String mrn) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientByMrn(mrn)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY','ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Patient updated", patientService.updatePatient(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success("Patient deleted", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PatientResponse>>> searchPatients(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<PatientResponse> result =
                patientService.searchPatients(search, status, doctorId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/advanced-search")
    public ResponseEntity<ApiResponse<PagedResponse<PatientResponse>>> advancedSearch(
            @RequestParam(required = false) String mrn,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String doctor,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String risk,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer age,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<PatientResponse> result = patientService.advancedSearch(
                mrn, name, phone, doctor, status, risk, gender, age, page, size);
        return ResponseEntity.ok(ApiResponse.success("Advanced search results retrieved successfully", result));
    }

    @GetMapping("/{id}/timeline")
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY','ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<List<TimelineEventResponse>>> getPatientTimeline(
            @PathVariable("id") Long id) {
        List<TimelineEventResponse> timeline = patientService.getPatientTimeline(id);
        return ResponseEntity.ok(ApiResponse.success("Patient timeline retrieved successfully", timeline));
    }

    @PatchMapping("/{id}/review-status")
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY','ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<PatientResponse>> updateReviewStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReviewStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PatientResponse response = patientService.updateReviewStatus(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Assessment review status updated successfully", response));
    }
}
