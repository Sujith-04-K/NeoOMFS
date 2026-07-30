package com.simats.neoomfs.controller;

import com.simats.neoomfs.dto.request.MedicalHistoryRequest;
import com.simats.neoomfs.dto.response.ApiResponse;
import com.simats.neoomfs.dto.response.MedicalHistoryResponse;
import com.simats.neoomfs.entity.MedicalHistory;
import com.simats.neoomfs.mapper.MedicalHistoryMapper;
import com.simats.neoomfs.service.impl.MedicalHistoryServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients/{id}/medical-history")
@RequiredArgsConstructor
public class MedicalHistoryController {

    private final MedicalHistoryServiceImpl medicalHistoryService;
    private final MedicalHistoryMapper medicalHistoryMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY','ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<MedicalHistoryResponse>> saveMedicalHistory(
            @PathVariable("id") Long patientId,
            @Valid @RequestBody MedicalHistoryRequest request) {
        MedicalHistory history = medicalHistoryService.saveOrUpdate(patientId, request);
        return ResponseEntity.ok(ApiResponse.success("Medical history saved successfully", medicalHistoryMapper.toResponse(history)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MedicalHistoryResponse>> getMedicalHistory(
            @PathVariable("id") Long patientId) {
        MedicalHistory history = medicalHistoryService.get(patientId);
        return ResponseEntity.ok(ApiResponse.success(medicalHistoryMapper.toResponse(history)));
    }
}
