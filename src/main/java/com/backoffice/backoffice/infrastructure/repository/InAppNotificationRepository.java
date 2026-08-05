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

    @SuppressWarnings("unchecked")
    public Object[] findByUserIdWithCount(UUID userId, int page, int size) {
        long start = System.currentTimeMillis();
        List<Object[]> results = getEntityManager().createNativeQuery(
                "SELECT n.id, n.notification_code, n.title, n.body, n.user_id, n.read, n.created_at, n.read_at, COUNT(*) OVER() " +
                "FROM notification.in_app_notification n " +
                "WHERE n.user_id = :userId " +
                "ORDER BY n.created_at DESC")
                .setParameter("userId", userId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        long duration = System.currentTimeMillis() - start;
        System.out.println("[PERF] findByUserIdWithCount (Native) -> duration: " + duration + " ms");
        
        long total = 0;
        List<InAppNotificationEntity> items = new java.util.ArrayList<>();
        for (Object[] row : results) {
            if (total == 0) total = ((Number) row[8]).longValue();
            InAppNotificationEntity e = new InAppNotificationEntity();
            e.setId((UUID) row[0]);
            e.setNotificationCode((String) row[1]);
            e.setTitle((String) row[2]);
            e.setBody((String) row[3]);
            e.setUserId((UUID) row[4]);
            e.setRead((Boolean) row[5]);
            e.setCreatedAt(((java.sql.Timestamp) row[6]).toInstant());
            if (row[7] != null) e.setReadAt(((java.sql.Timestamp) row[7]).toInstant());
            items.add(e);
        }
        return new Object[]{items, total};
    }

    public long countUnreadByUserId(UUID userId) {
        return count("userId = ?1 AND read = false", userId);
    }

    @SuppressWarnings("unchecked")
    public Object[] findByUserIdAndCategoryWithCount(UUID userId, String category, int page, int size) {
        long start = System.currentTimeMillis();
        List<Object[]> results = getEntityManager().createNativeQuery(
                "SELECT n.id, n.notification_code, n.title, n.body, n.user_id, n.read, n.created_at, n.read_at, COUNT(*) OVER() " +
                "FROM notification.in_app_notification n " +
                "JOIN notification.notification_definition d ON n.notification_code = d.code " +
                "WHERE n.user_id = :userId AND UPPER(d.category) = :category " +
                "ORDER BY n.created_at DESC")
                .setParameter("userId", userId)
                .setParameter("category", category.toUpperCase())
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        long duration = System.currentTimeMillis() - start;
        System.out.println("[PERF] findByUserIdAndCategoryWithCount (Native) -> duration: " + duration + " ms");
        
        long total = 0;
        List<InAppNotificationEntity> items = new java.util.ArrayList<>();
        for (Object[] row : results) {
            if (total == 0) total = ((Number) row[8]).longValue();
            InAppNotificationEntity e = new InAppNotificationEntity();
            e.setId((UUID) row[0]);
            e.setNotificationCode((String) row[1]);
            e.setTitle((String) row[2]);
            e.setBody((String) row[3]);
            e.setUserId((UUID) row[4]);
            e.setRead((Boolean) row[5]);
            e.setCreatedAt(((java.sql.Timestamp) row[6]).toInstant());
            if (row[7] != null) e.setReadAt(((java.sql.Timestamp) row[7]).toInstant());
            items.add(e);
        }
        return new Object[]{items, total};
    }

    public long countUnreadByUserIdAndCategory(UUID userId, String category) {
        long start = System.currentTimeMillis();
        Number count = (Number) getEntityManager().createNativeQuery(
                "SELECT COUNT(*) FROM notification.in_app_notification n " +
                "JOIN notification.notification_definition d ON n.notification_code = d.code " +
                "WHERE n.user_id = :userId AND n.read = false AND UPPER(d.category) = :category")
                .setParameter("userId", userId)
                .setParameter("category", category.toUpperCase())
                .getSingleResult();
        long duration = System.currentTimeMillis() - start;
        System.out.println("[PERF] countUnreadByUserIdAndCategory (Native) -> duration: " + duration + " ms");
        return count.longValue();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> countUnreadGroupedByCategory(UUID userId) {
        long start = System.currentTimeMillis();
        List<Object[]> results = getEntityManager().createNativeQuery(
                "SELECT d.category, COUNT(n.id) FROM notification.in_app_notification n " +
                "JOIN notification.notification_definition d ON n.notification_code = d.code " +
                "WHERE n.user_id = :userId AND n.read = false " +
                "GROUP BY d.category")
                .setParameter("userId", userId)
                .getResultList();
        long duration = System.currentTimeMillis() - start;
        System.out.println("[PERF] countUnreadGroupedByCategory (Native) -> duration: " + duration + " ms");
        return results;
    }
}