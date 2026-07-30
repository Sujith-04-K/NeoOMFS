package com.simats.neoomfs.controller;

import com.simats.neoomfs.dto.request.DentalExaminationRequest;
import com.simats.neoomfs.dto.response.ApiResponse;
import com.simats.neoomfs.dto.response.DentalExaminationResponse;
import com.simats.neoomfs.entity.DentalExamination;
import com.simats.neoomfs.mapper.DentalExaminationMapper;
import com.simats.neoomfs.service.impl.DentalExaminationServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients/{id}/dental")
@RequiredArgsConstructor
public class DentalExaminationController {

    private final DentalExaminationServiceImpl dentalService;
    private final DentalExaminationMapper dentalMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY','ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<DentalExaminationResponse>> saveDental(
            @PathVariable("id") Long patientId,
            @Valid @RequestBody DentalExaminationRequest request) {
        DentalExamination dental = dentalService.saveOrUpdate(patientId, request);
        return ResponseEntity.ok(ApiResponse.success("Dental examination saved successfully", dentalMapper.toResponse(dental)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DentalExaminationResponse>> getDental(
            @PathVariable("id") Long patientId) {
        DentalExamination dental = dentalService.get(patientId);
        return ResponseEntity.ok(ApiResponse.success(dentalMapper.toResponse(dental)));
    }
}
