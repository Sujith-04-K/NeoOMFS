package com.simats.neoomfs.service;

import com.simats.neoomfs.dto.response.NotificationResponse;
import com.simats.neoomfs.dto.response.PagedResponse;
import com.simats.neoomfs.entity.User;

public interface NotificationService {
    void createNotification(User recipient, String message, String type, Long referenceId, String referenceType);
    PagedResponse<NotificationResponse> getUserNotifications(String email, int page, int size);
    long getUnreadCount(String email);
    void markAllAsRead(String email);
    void markAsRead(Long id, String email);
}
