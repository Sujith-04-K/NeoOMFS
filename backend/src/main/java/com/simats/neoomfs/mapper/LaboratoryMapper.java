package com.simats.neoomfs.mapper;

import com.simats.neoomfs.dto.response.LaboratoryResponse;
import com.simats.neoomfs.entity.LaboratoryInvestigations;
import org.springframework.stereotype.Component;

@Component
public class LaboratoryMapper {

    public LaboratoryResponse toResponse(LaboratoryInvestigations lab) {
        if (lab == null) return null;
        return LaboratoryResponse.builder()
                .id(lab.getId())
                .patientId(lab.getPatient().getId())
                .hemoglobin(lab.getHemoglobin())
                .totalWbcCount(lab.getTotalWbcCount())
                .plateletCount(lab.getPlateletCount())
                .bleedingTime(lab.getBleedingTime())
                .clottingTime(lab.getClottingTime())
                .pt(lab.getPt())
                .inr(lab.getInr())
                .aptt(lab.getAptt())
                .fastingBloodSugar(lab.getFastingBloodSugar())
                .randomBloodSugar(lab.getRandomBloodSugar())
                .hba1c(lab.getHba1c())
                .bloodUrea(lab.getBloodUrea())
                .serumCreatinine(lab.getSerumCreatinine())
                .serumBilirubinTotal(lab.getSerumBilirubinTotal())
                .sgot(lab.getSgot())
                .sgpt(lab.getSgpt())
                .bloodGroup(lab.getBloodGroup())
                .rhFactor(lab.getRhFactor())
                .hivStatus(lab.getHivStatus())
                .hbsagStatus(lab.getHbsagStatus())
                .hcvStatus(lab.getHcvStatus())
                .labReportFileUrl(lab.getLabReportFileUrl())
                .notes(lab.getNotes())
                .build();
    }
}
