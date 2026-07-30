package com.simats.neoomfs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class AuditConfig {
    // Configures JPA auditing to automatically update @CreatedDate and @LastModifiedDate fields
}
