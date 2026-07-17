package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.domain.notification.InAppNotificationEntity;
import com.backoffice.backoffice.infrastructure.repository.InAppNotificationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class ReadNotificationUseCaseImpl implements ReadNotificationUseCase {

    private static final Logger LOG = Logger.getLogger(ReadNotificationUseCaseImpl.class);

    @Inject
    InAppNotificationRepository repository;

    @Override
    @Transactional
    public InAppNotificationEntity readNotification(UUID notificationId, UUID userId) {
        // Atomic UPDATE en DB - evita race conditions y garantiza persistencia inmediata
        int updated = repository.update(
                "set read = true, readAt = ?1 WHERE id = ?2 AND userId = ?3 AND read = false",
                Instant.now(), notificationId, userId
        );

        if (updated == 1) {
            LOG.infof("[NOTIFICATION] Marked as read: notificationId=%s, userId=%s", notificationId, userId);
        } else {
            InAppNotificationEntity entity = repository.findByIdOptional(notificationId)
                    .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
            if (!entity.getUserId().equals(userId)) {
                throw new SecurityException("Notification does not belong to user");
            }
            LOG.infof("[NOTIFICATION] Already read: notificationId=%s, userId=%s", notificationId, userId);
        }

        return repository.findByIdOptional(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found after update"));
    }
}