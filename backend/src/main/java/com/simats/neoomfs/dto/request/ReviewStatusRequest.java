package com.simats.neoomfs.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewStatusRequest {

    @NotBlank(message = "Status is required")
    private String status; // PENDING_REVIEW | APPROVED | NEEDS_REVISION

    private String reviewComments;
}
