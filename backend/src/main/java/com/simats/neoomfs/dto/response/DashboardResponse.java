package com.simats.neoomfs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dashboard summary response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private Long totalPatients;

    private Long todayPatients;

    private Long pendingClinicalDecision;

    private Long fitPatients;

    private Long reviewPatients;

    private Long unfitPatients;

    private Long reportsGenerated;

    private Long highRiskPatients;
}