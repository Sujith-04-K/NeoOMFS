package com.simats.neoomfs.dto.response;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsResponse {
    private long totalPatients;
    private long patientsThisMonth;
    private long reportsGenerated;
    private double averageAge;
    private long maleCount;
    private long femaleCount;
    private String maleFemaleRatio;
    private Map<String, Long> riskDistribution;
    private Map<String, Long> fitnessDecisionDistribution;
}
