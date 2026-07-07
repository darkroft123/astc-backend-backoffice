package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.domain.notification.InAppNotificationEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

public interface GetNotificationsUseCase {
    List<InAppNotificationEntity> getByUserId(UUID userId, int page, int size);
    long countUnread(UUID userId);
    List<InAppNotificationEntity> findByUserIdAndCategory(UUID userId, String category, int page, int size);
    long countByUserIdAndCategory(UUID userId, String category);
    long countUnreadByCategory(UUID userId, String category);
}