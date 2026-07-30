package com.simats.neoomfs.service.email;

import com.simats.neoomfs.exception.MailDeliveryException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    @Value("${app.mail.from:no-reply@neoomfs.local}")
    private String fromEmail;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Value("${app.password-reset.base-url:neoomfs://reset-password}")
    private String passwordResetBaseUrl;

    @PostConstruct
    void logMailConfiguration() {
        String normalizedPassword = normalizeAppPassword(smtpPassword);
        boolean passwordLooksPresent = !normalizedPassword.isBlank();
        boolean passwordHadWhitespace = !smtpPassword.equals(normalizedPassword);
        log.info("Password reset mail enabled: {} | from: {} | smtp user configured: {} | smtp password present: {} | smtp password had whitespace: {}",
                mailEnabled,
                fromEmail,
                !smtpUsername.isBlank(),
                passwordLooksPresent,
                passwordHadWhitespace);
    }

    public void sendPasswordResetEmail(String toEmail, String fullName, String otp) {
        if (!mailEnabled) {
            throw new MailDeliveryException(
                    "Password reset email is disabled. Set app.mail.enabled=true and configure MAIL_USERNAME / MAIL_PASSWORD."
            );
        }

        String normalizedPassword = normalizeAppPassword(smtpPassword);
        if (smtpUsername.isBlank() || normalizedPassword.isBlank()) {
            throw new MailDeliveryException("SMTP is enabled but credentials are incomplete. Configure spring.mail.username and spring.mail.password.");
        }

        if (!smtpPassword.equals(normalizedPassword)) {
            log.warn("spring.mail.password contains whitespace. Gmail app passwords should be stored without spaces.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Neo OMFS Password Reset OTP");
            message.setText(buildBody(fullName, otp));
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (MailException ex) {
            String causeMessage = ex.getMostSpecificCause() != null
                    ? ex.getMostSpecificCause().getMessage()
                    : ex.getMessage();
            log.error("SMTP failure while sending password reset email to {}: {}", toEmail, causeMessage, ex);
            throw new MailDeliveryException(
                    "Unable to send password reset email. SMTP error: " + causeMessage,
                    ex
            );
        }
    }

    private String normalizeAppPassword(String password) {
        return password == null ? "" : WHITESPACE.matcher(password).replaceAll("");
    }

    private String buildBody(String fullName, String otp) {
        return "Hello " + fullName + ",\n\n" +
                "We received a request to reset your Neo OMFS password.\n\n" +
                "Your 6-digit OTP is:\n" +
                otp + "\n\n" +
                "Enter this OTP in the Neo OMFS app to set a new password.\n\n" +
                "This OTP expires in 15 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Neo OMFS Support";
    }
}
