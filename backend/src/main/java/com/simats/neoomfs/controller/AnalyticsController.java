package com.simats.neoomfs.controller;

import com.simats.neoomfs.dto.response.ApiResponse;
import com.simats.neoomfs.dto.response.AnalyticsResponse;
import com.simats.neoomfs.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for fetching clinic metrics and demographics stats")
@PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY','ROLE_STUDENT')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get clinics clinical & demographic analytics")
    @GetMapping
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics() {
        AnalyticsResponse response = analyticsService.getAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Analytics retrieved successfully", response));
    }
}
