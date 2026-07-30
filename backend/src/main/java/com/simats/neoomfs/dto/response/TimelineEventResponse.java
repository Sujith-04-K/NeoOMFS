package com.simats.neoomfs.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimelineEventResponse {
    private String event;
    private String description;
    private String performedBy;
    private LocalDateTime timestamp;
}
