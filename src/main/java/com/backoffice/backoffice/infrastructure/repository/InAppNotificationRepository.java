package com.backoffice.backoffice.infrastructure.repository;

import com.backoffice.backoffice.domain.notification.InAppNotificationEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class InAppNotificationRepository implements PanacheRepositoryBase<InAppNotificationEntity, UUID> {

    private static final Logger LOG = Logger.getLogger(InAppNotificationRepository.class);

    public List<InAppNotificationEntity> findByUserId(UUID userId, int page, int size) {
        return find("userId = ?1 ORDER BY createdAt DESC", userId)
                .page(page, size)
                .list();
    }

    public long countByUserId(UUID userId) {
        return count("userId = ?1", userId);
    }

    public long countUnreadByUserId(UUID userId) {
        return count("userId = ?1 AND read = false", userId);
    }

    @SuppressWarnings("unchecked")
    public List<InAppNotificationEntity> findByUserIdAndCategory(UUID userId, String category, int page, int size) {
        return getEntityManager().createNativeQuery(
                "SELECT n.* FROM notification.in_app_notification n " +
                "JOIN notification.notification_definition d ON n.notification_code = d.code " +
                "WHERE n.user_id = :userId AND d.category = :category " +
                "ORDER BY n.created_at DESC", InAppNotificationEntity.class)
                .setParameter("userId", userId)
                .setParameter("category", category.toUpperCase())
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public long countByUserIdAndCategory(UUID userId, String category) {
        Number count = (Number) getEntityManager().createNativeQuery(
                "SELECT COUNT(*) FROM notification.in_app_notification n " +
                "JOIN notification.notification_definition d ON n.notification_code = d.code " +
                "WHERE n.user_id = :userId AND d.category = :category")
                .setParameter("userId", userId)
                .setParameter("category", category.toUpperCase())
                .getSingleResult();
        return count.longValue();
    }

    public long countUnreadByUserIdAndCategory(UUID userId, String category) {
        Number count = (Number) getEntityManager().createNativeQuery(
                "SELECT COUNT(*) FROM notification.in_app_notification n " +
                "JOIN notification.notification_definition d ON n.notification_code = d.code " +
                "WHERE n.user_id = :userId AND n.read = false AND d.category = :category")
                .setParameter("userId", userId)
                .setParameter("category", category.toUpperCase())
                .getSingleResult();
        return count.longValue();
    }
}