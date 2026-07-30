package com.simats.neoomfs.controller;

import com.simats.neoomfs.dto.request.RadiologyRequest;
import com.simats.neoomfs.dto.response.ApiResponse;
import com.simats.neoomfs.dto.response.RadiologyResponse;
import com.simats.neoomfs.entity.Radiology;
import com.simats.neoomfs.mapper.RadiologyMapper;
import com.simats.neoomfs.service.impl.RadiologyServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients/{id}/radiology")
@RequiredArgsConstructor
public class RadiologyController {

    private final RadiologyServiceImpl radiologyService;
    private final RadiologyMapper radiologyMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY','ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<RadiologyResponse>> saveRadiology(
            @PathVariable("id") Long patientId,
            @Valid @RequestBody RadiologyRequest request) {
        Radiology radiology = radiologyService.saveOrUpdate(patientId, request);
        return ResponseEntity.ok(ApiResponse.success("Radiology saved successfully", radiologyMapper.toResponse(radiology)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<RadiologyResponse>> getRadiology(
            @PathVariable("id") Long patientId) {
        Radiology radiology = radiologyService.get(patientId);
        return ResponseEntity.ok(ApiResponse.success(radiologyMapper.toResponse(radiology)));
    }
}
