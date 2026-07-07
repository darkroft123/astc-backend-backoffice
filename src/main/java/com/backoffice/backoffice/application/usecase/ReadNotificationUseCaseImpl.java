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
        InAppNotificationEntity entity = repository.findByIdOptional(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!entity.getUserId().equals(userId)) {
            throw new SecurityException("Notification does not belong to user");
        }

        if (!entity.getRead()) {
            entity.setRead(true);
            entity.setReadAt(Instant.now());
            LOG.infof("[NOTIFICATION] Marked as read: notificationId=%s, userId=%s", notificationId, userId);
        } else {
            LOG.infof("[NOTIFICATION] Already read: notificationId=%s, userId=%s", notificationId, userId);
        }

        return entity;
    }
}