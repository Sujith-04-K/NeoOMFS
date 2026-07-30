package com.simats.neoomfs.dto.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Inbound DTO for laboratory investigations (Step 4 of the wizard).
 */
@Data
public class LaboratoryRequest {
    private BigDecimal hemoglobin;
    private Integer totalWbcCount;
    private Integer plateletCount;
    private BigDecimal bleedingTime;
    private BigDecimal clottingTime;
    private BigDecimal pt;
    private BigDecimal inr;
    private BigDecimal aptt;
    private BigDecimal fastingBloodSugar;
    private BigDecimal randomBloodSugar;
    private BigDecimal hba1c;
    private BigDecimal bloodUrea;
    private BigDecimal serumCreatinine;
    private BigDecimal serumBilirubinTotal;
    private BigDecimal sgot;
    private BigDecimal sgpt;
    private String bloodGroup;
    private String rhFactor;
    private String hivStatus;
    private String hbsagStatus;
    private String hcvStatus;
    private String labReportFileUrl;
    private String notes;
}
