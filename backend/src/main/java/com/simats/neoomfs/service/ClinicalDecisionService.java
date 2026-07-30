package com.simats.neoomfs.service;

import com.simats.neoomfs.dto.request.ClinicalDecisionRequest;
import com.simats.neoomfs.dto.response.ClinicalDecisionResponse;

public interface ClinicalDecisionService {
    ClinicalDecisionResponse evaluateFitness(Long patientId, String doctorEmail);
    ClinicalDecisionResponse getDecision(Long patientId);
    ClinicalDecisionResponse saveCustomNotes(Long patientId, ClinicalDecisionRequest request);
}
