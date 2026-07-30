package com.simats.neoomfs.controller;

import com.simats.neoomfs.dto.response.ApiResponse;
import com.simats.neoomfs.dto.response.NotificationResponse;
import com.simats.neoomfs.dto.response.PagedResponse;
import com.simats.neoomfs.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<NotificationResponse> response =
                notificationService.getUserNotifications(userDetails.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = notificationService.getUnreadCount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> readAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.<Void>success("All notifications marked as read", null));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> readNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAsRead(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.<Void>success("Notification marked as read", null));
    }
}
