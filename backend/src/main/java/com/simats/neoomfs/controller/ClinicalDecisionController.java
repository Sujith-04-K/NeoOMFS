package com.simats.neoomfs.controller;

import com.simats.neoomfs.dto.request.ClinicalDecisionRequest;
import com.simats.neoomfs.dto.response.ApiResponse;
import com.simats.neoomfs.dto.response.ClinicalDecisionResponse;
import com.simats.neoomfs.service.ClinicalDecisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients/{id}/decision")
@RequiredArgsConstructor
public class ClinicalDecisionController {

    private final ClinicalDecisionService clinicalDecisionService;

    @PostMapping("/evaluate")
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY')")
    public ResponseEntity<ApiResponse<ClinicalDecisionResponse>> evaluate(
            @PathVariable("id") Long patientId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ClinicalDecisionResponse response =
                clinicalDecisionService.evaluateFitness(patientId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Patient fitness evaluation completed.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ClinicalDecisionResponse>> getDecision(
            @PathVariable("id") Long patientId) {
        ClinicalDecisionResponse response = clinicalDecisionService.getDecision(patientId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/notes")
    @PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY')")
    public ResponseEntity<ApiResponse<ClinicalDecisionResponse>> saveNotes(
            @PathVariable("id") Long patientId,
            @Valid @RequestBody ClinicalDecisionRequest request) {
        ClinicalDecisionResponse response = clinicalDecisionService.saveCustomNotes(patientId, request);
        return ResponseEntity.ok(ApiResponse.success("Custom clinical notes updated.", response));
    }
}
