package com.simats.neoomfs.decisionengine;

public interface ClinicalRule {
    void evaluate(PatientDataContext context);
}
