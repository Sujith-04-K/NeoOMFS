package com.simats.neoomfs.util;

import com.simats.neoomfs.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class MrnGenerator {

    private final PatientRepository patientRepository;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PREFIX = "MRN";

    public String generateUniqueMrn() {
        String mrn;
        int attempts = 0;
        do {
            mrn = generateMrn();
            attempts++;
            if (attempts > 100) throw new IllegalStateException("Unable to generate unique MRN");
        } while (patientRepository.existsByMrn(mrn));
        return mrn;
    }

    private String generateMrn() {
        int number = 10000 + RANDOM.nextInt(90000); // 10000–99999
        return PREFIX + "-" + number;
    }
}
