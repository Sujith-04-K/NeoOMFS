package com.simats.neoomfs.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentReportResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private String patientMrn;
    private String reportFileName;
    private String downloadUrl;
    private LocalDateTime reportGeneratedAt;
    private Integer reportVersion;
    private String generatedByName;
}
