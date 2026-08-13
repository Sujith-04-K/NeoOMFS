package com.simats.neoomfs.config;

import com.simats.neoomfs.entity.Patient;
import com.simats.neoomfs.entity.Role;
import com.simats.neoomfs.entity.User;
import com.simats.neoomfs.repository.PatientRepository;
import com.simats.neoomfs.repository.RoleRepository;
import com.simats.neoomfs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * Automatically seeds default roles, institutional accounts, and clinical test cases on startup
 * if they do not exist. Ensures immediate testability in dev and test modes for both web and mobile.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking database seeding status...");

        // 1. Seed Roles
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Seeded role: {}", roleName);
            }
        }

        // 2. Seed Default Institutional Users
        seedUser("doctor@simats.ac.in", "dr.arvind", "Dr. Arvind Kumar", "Password@123", Role.RoleName.ROLE_DOCTOR, "MDS-OMFS-104");
        seedUser("student@simats.ac.in", "sujith.k", "Sujith K (BDS/MDS Student)", "Password@123", Role.RoleName.ROLE_STUDENT, "STU-2024-098");
        seedUser("faculty@simats.ac.in", "dr.lakshmi", "Dr. Lakshmi Raman (Head Faculty)", "Password@123", Role.RoleName.ROLE_FACULTY, "MDS-OMFS-001");
        seedUser("admin@simats.ac.in", "admin", "System Administrator", "Password@123", Role.RoleName.ROLE_ADMIN, "ADM-001");

        // 3. Seed Demo Clinical Patient Cases (if empty)
        if (patientRepository.count() == 0) {
            seedPatients();
        }

        log.info("Database seeding verification complete.");
    }

    private void seedPatients() {
        Optional<User> doctorOpt = userRepository.findByEmail("doctor@simats.ac.in");
        Optional<User> facultyOpt = userRepository.findByEmail("faculty@simats.ac.in");
        User creator = doctorOpt.orElse(null);
        User reviewer = facultyOpt.orElse(null);

        // Case 1: Pending Review
        Patient p1 = Patient.builder()
                .mrn("SIM-2024-001")
                .fullName("Rajesh Sharma")
                .age(28)
                .dateOfBirth(LocalDate.of(1996, 5, 14))
                .gender("Male")
                .bloodGroup("O+")
                .phoneNumber("9876543210")
                .address("Chennai, Tamil Nadu")
                .procedureType("Surgical Removal of Impaction (38)")
                .referringDoctor(creator)
                .assessmentStatus(Patient.AssessmentStatus.PENDING_REVIEW)
                .submittedBy("Sujith K (BDS/MDS Student)")
                .createdBy(creator)
                .build();
        patientRepository.save(p1);

        // Case 2: Approved
        Patient p2 = Patient.builder()
                .mrn("SIM-2024-002")
                .fullName("Ananya Iyer")
                .age(45)
                .dateOfBirth(LocalDate.of(1979, 11, 20))
                .gender("Female")
                .bloodGroup("B+")
                .phoneNumber("9841001234")
                .address("Adyar, Chennai")
                .procedureType("Enucleation of Odontogenic Cyst")
                .referringDoctor(creator)
                .assessmentStatus(Patient.AssessmentStatus.APPROVED)
                .submittedBy("Dr. Arvind Kumar")
                .reviewedBy(reviewer)
                .reviewComments("Fit for surgery under LA with sedation. Airway and vitals within normal limits.")
                .approvedAt(LocalDateTime.now().minusDays(1))
                .createdBy(creator)
                .build();
        patientRepository.save(p2);

        // Case 3: Needs Revision (High risk example)
        Patient p3 = Patient.builder()
                .mrn("SIM-2024-003")
                .fullName("Venkatesh Rao")
                .age(62)
                .dateOfBirth(LocalDate.of(1962, 2, 10))
                .gender("Male")
                .bloodGroup("A+")
                .phoneNumber("9791005678")
                .address("T. Nagar, Chennai")
                .procedureType("Bilateral Third Molar Extraction")
                .referringDoctor(creator)
                .assessmentStatus(Patient.AssessmentStatus.NEEDS_REVISION)
                .submittedBy("Sujith K (BDS/MDS Student)")
                .reviewedBy(reviewer)
                .reviewComments("High BP noted (170/100 mmHg). ASA III risk. Please repeat BP after physician consultation and update coagulation profile before re-submitting.")
                .createdBy(creator)
                .build();
        patientRepository.save(p3);

        log.info("Seeded 3 demo clinical patient cases into shared H2 database.");
    }

    private void seedUser(String email, String username, String fullName, String rawPassword, Role.RoleName roleName, String licenseNumber) {
        if (userRepository.findByEmail(email).isEmpty()) {
            Optional<Role> roleOpt = roleRepository.findByName(roleName);
            if (roleOpt.isPresent()) {
                User user = new User();
                user.setEmail(email);
                user.setUsername(username);
                user.setFullName(fullName);
                user.setPassword(passwordEncoder.encode(rawPassword));
                user.setLicenseNumber(licenseNumber);
                user.setRoles(Set.of(roleOpt.get()));
                user.setActive(true);
                userRepository.save(user);
                log.info("Seeded user account: {} [{}]", email, roleName);
            }
        }
    }
}
