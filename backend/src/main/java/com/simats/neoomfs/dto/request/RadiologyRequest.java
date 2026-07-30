package com.simats.neoomfs.dto.request;

import lombok.Data;

/**
 * Inbound DTO for radiology data (Step 3 of the wizard).
 */
@Data
public class RadiologyRequest {
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
