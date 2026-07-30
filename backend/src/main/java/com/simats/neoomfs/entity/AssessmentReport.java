package com.simats.neoomfs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AssessmentReport entity – Step 8 metadata.
 * Tracks the generated PDF report file path and generation details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "assessment_reports")
public class AssessmentReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "report_file_path", length = 500)
    private String reportFilePath;

    @Column(name = "report_file_name", length = 200)
    private String reportFileName;

    @Column(name = "report_generated_at")
    private LocalDateTime reportGeneratedAt;

    @Column(name = "report_version")
    @Builder.Default
    private Integer reportVersion = 1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "generated_by_user_id")
    private User generatedBy;
}