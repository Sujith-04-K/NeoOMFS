package com.simats.neoomfs.controller;

import com.simats.neoomfs.dto.request.LaboratoryRequest;
import com.simats.neoomfs.dto.response.ApiResponse;
import com.simats.neoomfs.dto.response.LaboratoryResponse;
import com.simats.neoomfs.entity.LaboratoryInvestigations;
import com.simats.neoomfs.mapper.LaboratoryMapper;
import com.simats.neoomfs.service.impl.LaboratoryServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients/{id}/laboratory")
@RequiredArgsConstructor
public class LaboratoryController {

    private final LaboratoryServiceImpl laboratoryService;
    private final LaboratoryMapper laboratoryMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY','ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<LaboratoryResponse>> saveLaboratory(
            @PathVariable("id") Long patientId,
            @Valid @RequestBody LaboratoryRequest request) {
        LaboratoryInvestigations lab = laboratoryService.saveOrUpdate(patientId, request);
        return ResponseEntity.ok(ApiResponse.success("Laboratory investigations saved successfully", laboratoryMapper.toResponse(lab)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<LaboratoryResponse>> getLaboratory(
            @PathVariable("id") Long patientId) {
        LaboratoryInvestigations lab = laboratoryService.get(patientId);
        return ResponseEntity.ok(ApiResponse.success(laboratoryMapper.toResponse(lab)));
    }
}
