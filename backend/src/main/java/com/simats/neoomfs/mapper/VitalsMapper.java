package com.simats.neoomfs.mapper;

import com.simats.neoomfs.dto.response.PatientVitalsResponse;
import com.simats.neoomfs.entity.PatientVitals;
import org.springframework.stereotype.Component;

@Component
public class VitalsMapper {

    public PatientVitalsResponse toResponse(PatientVitals vitals) {
        if (vitals == null) return null;
        return PatientVitalsResponse.builder()
                .id(vitals.getId())
                .patientId(vitals.getPatient().getId())
                .bpSystolic(vitals.getBpSystolic())
                .bpDiastolic(vitals.getBpDiastolic())
                .pulseRate(vitals.getPulseRate())
                .temperature(vitals.getTemperature())
                .spo2(vitals.getSpo2())
                .respiratoryRate(vitals.getRespiratoryRate())
                .heightCm(vitals.getHeightCm())
                .weightKg(vitals.getWeightKg())
                .bmi(vitals.getBmi())
                .randomBloodSugar(vitals.getRandomBloodSugar())
                .notes(vitals.getNotes())
                .build();
    }
}
