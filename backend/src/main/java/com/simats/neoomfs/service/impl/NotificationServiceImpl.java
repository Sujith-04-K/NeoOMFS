package com.simats.neoomfs.service.impl;

import com.simats.neoomfs.dto.response.NotificationResponse;
import com.simats.neoomfs.dto.response.PagedResponse;
import com.simats.neoomfs.entity.Notification;
import com.simats.neoomfs.entity.User;
import com.simats.neoomfs.exception.ForbiddenException;
import com.simats.neoomfs.exception.ResourceNotFoundException;
import com.simats.neoomfs.repository.NotificationRepository;
import com.simats.neoomfs.repository.UserRepository;
import com.simats.neoomfs.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public void createNotification(User recipient, String message, String type, Long referenceId, String referenceType) {
        Notification.NotificationType notifType;
        try {
            notifType = Notification.NotificationType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            notifType = Notification.NotificationType.INFO;
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .title("System Alert")
                .message(message)
                .type(notifType)
                .relatedPatientId(referenceId)
                .read(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getUserNotifications(String email, int page, int size) {
        User user = getUser(email);
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifPage = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable);

        List<NotificationResponse> content = notifPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<NotificationResponse>builder()
                .content(content)
                .page(notifPage.getNumber())
                .size(notifPage.getSize())
                .totalElements(notifPage.getTotalElements())
                .totalPages(notifPage.getTotalPages())
                .last(notifPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        User user = getUser(email);
        return notificationRepository.countByRecipientIdAndReadFalse(user.getId());
    }

    @Override
    public void markAllAsRead(String email) {
        User user = getUser(email);
        notificationRepository.markAllReadByUserId(user.getId());
    }

    @Override
    public void markAsRead(Long id, String email) {
        User user = getUser(email);
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        if (!notif.getRecipient().getId().equals(user.getId())) {
            throw new ForbiddenException("Cannot access notifications of other users");
        }
        notif.setRead(true);
        notificationRepository.save(notif);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType() != null ? n.getType().name() : null)
                .read(n.isRead())
                .relatedPatientId(n.getRelatedPatientId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
