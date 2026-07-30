package com.simats.neoomfs.controller;

import com.simats.neoomfs.dto.request.PatientVitalsRequest;
import com.simats.neoomfs.dto.response.ApiResponse;
import com.simats.neoomfs.dto.response.PatientVitalsResponse;
import com.simats.neoomfs.entity.PatientVitals;
import com.simats.neoomfs.mapper.VitalsMapper;
import com.simats.neoomfs.service.impl.VitalsServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients/{id}/vitals")
@RequiredArgsConstructor
public class VitalsController {

    private final VitalsServiceImpl vitalsService;
    private final VitalsMapper vitalsMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY','ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<PatientVitalsResponse>> saveVitals(
            @PathVariable("id") Long patientId,
            @Valid @RequestBody PatientVitalsRequest request) {
        PatientVitals vitals = vitalsService.saveOrUpdate(patientId, request);
        return ResponseEntity.ok(ApiResponse.success("Vitals saved successfully", vitalsMapper.toResponse(vitals)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PatientVitalsResponse>> getVitals(
            @PathVariable("id") Long patientId) {
        PatientVitals vitals = vitalsService.get(patientId);
        return ResponseEntity.ok(ApiResponse.success(vitalsMapper.toResponse(vitals)));
    }
}
