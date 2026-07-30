package com.simats.neoomfs.service;

import com.simats.neoomfs.dto.response.TimelineEventResponse;
import com.simats.neoomfs.entity.AuditLog;
import com.simats.neoomfs.entity.Patient;
import com.simats.neoomfs.entity.User;
import com.simats.neoomfs.repository.AuditLogRepository;
import com.simats.neoomfs.repository.PatientRepository;
import com.simats.neoomfs.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientTimelineTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    @Test
    public void testGetPatientTimeline() {
        Patient patient = Patient.builder().fullName("Jane Doe").mrn("MRN456").build();
        patient.setId(1L);

        when(patientRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(patient));

        User doctor = User.builder().fullName("Dr. Smith").username("drsmith").build();

        AuditLog log1 = AuditLog.builder()
                .action("CREATE")
                .description("Created patient record for MRN: MRN456")
                .username("drsmith")
                .user(doctor)
                .timestamp(LocalDateTime.now().minusHours(2))
                .build();

        AuditLog log2 = AuditLog.builder()
                .action("VITALS_SAVED")
                .description("Saved vitals for patient")
                .username("drsmith")
                .user(doctor)
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();

        Page<AuditLog> page = new PageImpl<>(Arrays.asList(log1, log2));
        when(auditLogRepository.findByPatientId(eq(1L), any(Pageable.class))).thenReturn(page);

        List<TimelineEventResponse> timeline = patientService.getPatientTimeline(1L);

        assertNotNull(timeline);
        assertEquals(2, timeline.size());

        assertEquals("Patient Registered", timeline.get(0).getEvent());
        assertEquals("Dr. Smith (drsmith)", timeline.get(0).getPerformedBy());

        assertEquals("Clinical Vitals Recorded", timeline.get(1).getEvent());
    }
}
