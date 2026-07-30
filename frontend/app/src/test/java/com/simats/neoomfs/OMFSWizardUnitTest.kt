package com.simats.neoomfs

import com.simats.neoomfs.models.ClinicalDecisionResponse
import com.simats.neoomfs.models.PatientVitalsRequest
import org.junit.Assert.*
import org.junit.Test

/**
 * Local unit tests verifying Android frontend data models and clinical boundary validation logic.
 */
class OMFSWizardUnitTest {

    @Test
    fun vitalsRequest_hypertensiveCrisis_isCorrectlyDetected() {
        val vitals = PatientVitalsRequest(
            bpSystolic = 185,
            bpDiastolic = 115,
            temperature = 37.0,
            pulseRate = 88,
            spo2 = 98.0,
            respiratoryRate = 16,
            heightCm = 175.0,
            weightKg = 70.0,
            bmi = 22.8,
            randomBloodSugar = 140.0,
            notes = "Patient experiencing severe headache"
        )

        val isHypertensiveCrisis = (vitals.bpSystolic ?: 0) >= 180 || (vitals.bpDiastolic ?: 0) >= 110
        val isStage2Hypertension = !isHypertensiveCrisis && ((vitals.bpSystolic ?: 0) >= 160 || (vitals.bpDiastolic ?: 0) >= 100)

        assertTrue("Should detect Hypertensive Crisis for BP 185/115", isHypertensiveCrisis)
        assertFalse("Should not flag as Stage 2 when in Crisis", isStage2Hypertension)
        assertEquals(22.8, vitals.bmi ?: 0.0, 0.01)
    }

    @Test
    fun vitalsRequest_normalVitals_isNotFlagged() {
        val vitals = PatientVitalsRequest(
            bpSystolic = 120,
            bpDiastolic = 80,
            temperature = 36.8,
            pulseRate = 72,
            spo2 = 99.0,
            respiratoryRate = 14,
            heightCm = 170.0,
            weightKg = 65.0,
            bmi = 22.5
        )

        val isHypertensiveCrisis = (vitals.bpSystolic ?: 0) >= 180 || (vitals.bpDiastolic ?: 0) >= 110
        val isHypoxemic = (vitals.spo2 ?: 100.0) < 95.0

        assertFalse("Normal BP 120/80 should not be hypertensive crisis", isHypertensiveCrisis)
        assertFalse("SpO2 99% should not be hypoxemic", isHypoxemic)
    }

    @Test
    fun clinicalDecisionResponse_riskClassification_isParsedCorrectly() {
        val response = ClinicalDecisionResponse(
            id = 101L,
            patientId = 505L,
            fitnessStatus = "CRITICAL",
            riskLevel = "VERY_HIGH",
            decisionNotes = "ASA Class IV patient with uncontrolled diabetes",
            redAlerts = listOf("ASA Class IV: Severe systemic disease", "Hypertensive Crisis (BP >= 180/110)"),
            yellowAlerts = listOf("Elevated Bleeding Risk (INR 1.8)"),
            recommendations = listOf("Defer elective surgery immediately", "Refer to Cardiology and Diabetologist")
        )

        assertEquals("CRITICAL", response.fitnessStatus)
        assertEquals("VERY_HIGH", response.riskLevel)
        assertEquals(2, response.redAlerts?.size)
        assertEquals(1, response.yellowAlerts?.size)
        assertTrue(response.recommendations?.contains("Defer elective surgery immediately") == true)
    }

    @Test
    fun asaClassification_riskLevelMapping_isCorrect() {
        val asaClass = "ASA_III"
        val expectedRiskLevel = when (asaClass.uppercase()) {
            "ASA_I", "ASA_1" -> "LOW"
            "ASA_II", "ASA_2" -> "MODERATE"
            "ASA_III", "ASA_3" -> "HIGH"
            "ASA_IV", "ASA_4" -> "VERY_HIGH"
            else -> "UNKNOWN"
        }

        assertEquals("HIGH", expectedRiskLevel)
    }
}
