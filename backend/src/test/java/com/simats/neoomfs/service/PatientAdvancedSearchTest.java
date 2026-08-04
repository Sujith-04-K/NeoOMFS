package com.simats.neoomfs.service;

import com.simats.neoomfs.dto.response.PagedResponse;
import com.simats.neoomfs.dto.response.PatientResponse;
import com.simats.neoomfs.entity.Patient;
import com.simats.neoomfs.entity.ClinicalDecision;
import com.simats.neoomfs.repository.PatientRepository;
import com.simats.neoomfs.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientAdvancedSearchTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    @Test
    public void testAdvancedSearch() {
        Patient patient = Patient.builder()
                .fullName("Alice Smith")
                .mrn("MRN789")
                .age(25)
                .gender("Female")
                .assessmentStatus(Patient.AssessmentStatus.APPROVED)
                .build();
        patient.setId(10L);

        Page<Patient> page = new PageImpl<>(Collections.singletonList(patient));
        
        when(patientRepository.advancedSearch(
                any(), any(), any(), any(),
                any(), any(),
                any(), any(), any(Pageable.class)
        )).thenReturn(page);

        PagedResponse<PatientResponse> response = patientService.advancedSearch(
                "MRN789", "Alice", "123", "doctor",
                "FIT", "LOW", "Female", 25, 0, 10
        );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Alice Smith", response.getContent().get(0).getFullName());
        assertEquals("MRN789", response.getContent().get(0).getMrn());
    }
}
