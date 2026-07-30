package com.simats.neoomfs.service;

import com.simats.neoomfs.dto.response.AuditLogResponse;
import com.simats.neoomfs.dto.response.PagedResponse;
import com.simats.neoomfs.entity.AuditLog;
import com.simats.neoomfs.entity.Patient;
import com.simats.neoomfs.entity.User;
import com.simats.neoomfs.repository.AuditLogRepository;
import com.simats.neoomfs.repository.UserRepository;
import com.simats.neoomfs.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testLog_withExplicitUser() {
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);

        auditLogService.log(1L, "admin", 2L, "PATIENT", "CREATE", "Created patient", "Patient", 2L);

        verify(auditLogRepository, times(1)).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals("admin", saved.getUsername());
        assertEquals(2L, saved.getPatientId());
        assertEquals("PATIENT", saved.getModule());
        assertEquals("CREATE", saved.getAction());
        assertEquals("Created patient", saved.getDescription());
        assertNotNull(saved.getTimestamp());
    }

    @Test
    public void testLog_withSecurityContextUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("doctor@simats.edu");

        User user = User.builder().username("doctor").build();
        user.setId(10L);
        when(userRepository.findByEmail("doctor@simats.edu")).thenReturn(Optional.of(user));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);

        auditLogService.log(null, null, 5L, "VITALS", "VITALS_SAVED", "Saved vitals", "PatientVitals", 1L);

        verify(auditLogRepository, times(1)).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertEquals(10L, saved.getUserId());
        assertEquals("doctor", saved.getUsername());
        assertEquals(5L, saved.getPatientId());
        assertEquals("VITALS", saved.getModule());
        assertEquals("VITALS_SAVED", saved.getAction());
    }

    @Test
    public void testGetAuditLogsByPatient() {
        Patient patient = Patient.builder().fullName("John Doe").mrn("MRN123").build();
        patient.setId(5L);

        AuditLog logEntry = AuditLog.builder()
                .id(1L)
                .userId(10L)
                .username("doctor")
                .patientId(5L)
                .patient(patient)
                .module("PATIENT")
                .action("CREATE")
                .description("Created patient")
                .ipAddress("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        Page<AuditLog> page = new PageImpl<>(Collections.singletonList(logEntry));
        when(auditLogRepository.findByPatientId(eq(5L), any(Pageable.class))).thenReturn(page);

        PagedResponse<AuditLogResponse> response = auditLogService.getAuditLogsByPatient(5L, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        AuditLogResponse dto = response.getContent().get(0);
        assertEquals("John Doe", dto.getPatientName());
        assertEquals("MRN123", dto.getPatientMrn());
        assertEquals("PATIENT", dto.getModule());
    }
}
