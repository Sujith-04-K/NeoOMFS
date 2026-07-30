package com.simats.neoomfs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RadiologyResponse {
    private Long id;
    private Long patientId;
    private boolean iopaTaken;
    private String iopaFileUrl;
    private String iopaFindings;
    private boolean opgTaken;
    private String opgFileUrl;
    private String opgFindings;
    private boolean cbctTaken;
    private String cbctFileUrl;
    private String cbctFindings;
    private Double boneDensityHu;
    private String generalRadiologyNotes;
}
