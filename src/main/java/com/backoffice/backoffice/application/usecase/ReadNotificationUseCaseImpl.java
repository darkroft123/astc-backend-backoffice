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
        long start = System.currentTimeMillis();
        repository.markAsRead(notificationId, userId);
        InAppNotificationEntity entity = repository.findByIdAndUserId(notificationId, userId);
        if (entity == null) {
            throw new SecurityException("Notification not found or does not belong to user");
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("[PERF] readNotification -> duration: " + duration + " ms");
        return entity;
    }
}
