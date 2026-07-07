package com.backoffice.backoffice.infrastructure.controller.graphql;

import com.backoffice.backoffice.application.usecase.GetNotificationsUseCase;
import com.backoffice.backoffice.application.usecase.ReadNotificationUseCase;
import com.backoffice.backoffice.domain.notification.InAppNotificationEntity;
import com.backoffice.backoffice.infrastructure.controller.graphql.dto.InAppNotificationCategoryUnreadCountResponse;
import com.backoffice.backoffice.infrastructure.controller.graphql.dto.InAppNotificationResponse;
import com.backoffice.backoffice.infrastructure.controller.graphql.dto.PaginationResponse;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.*;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GraphQLApi
public class InAppNotificationResource {

    private static final Logger LOG = Logger.getLogger(InAppNotificationResource.class);

    @Inject
    GetNotificationsUseCase getNotificationsUseCase;

    @Inject
    ReadNotificationUseCase readNotificationUseCase;

    @Query("GetAllNotifications")
    @Description("Get notifications for a user with pagination")
    public Uni<List<InAppNotificationResponse>> getNotifications(
            @Name("userId") String userId,
            @Name("page") @DefaultValue("0") int page,
            @Name("size") @DefaultValue("10") int size) {
        return Uni.createFrom().item(() -> {
            LOG.infof("GraphQL Query GetAllNotifications: userId=%s, page=%d, size=%d", userId, page, size);
            List<InAppNotificationEntity> entities = getNotificationsUseCase.getByUserId(UUID.fromString(userId), page, size);
            return entities.stream()
                    .map(InAppNotificationResponse::fromEntity)
                    .collect(Collectors.toList());
        });
    }

    @Query("GetUnreadCount")
    @Description("Get count of unread notifications for a user, optionally filtered by category")
    public Uni<Long> getUnreadCount(
            @Name("userId") String userId,
            @Name("category") @DefaultValue("") String category) {
        return Uni.createFrom().item(() -> {
            LOG.infof("GraphQL Query GetUnreadCount: userId=%s, category=%s", userId, category);
            if (category == null || category.isBlank()) {
                return getNotificationsUseCase.countUnread(UUID.fromString(userId));
            } else {
                return getNotificationsUseCase.countUnreadByCategory(UUID.fromString(userId), category);
            }
        });
    }

    @Query("GetNotificationsByCategory")
    @Description("Get notifications for a user filtered by category with pagination")
    public Uni<PaginationResponse> getNotificationsByCategory(
            @Name("userId") String userId,
            @Name("category") String category,
            @Name("page") @DefaultValue("1") int page,
            @Name("size") @DefaultValue("10") int size) {
        int internalPage = Math.max(page - 1, 0);
        UUID userUUID = UUID.fromString(userId);
        return Uni.createFrom().item(() -> {
            LOG.infof("GraphQL Query GetNotificationsByCategory: userId=%s, category=%s, page=%d, size=%d", userId, category, page, size);
            List<InAppNotificationEntity> items = getNotificationsUseCase.findByUserIdAndCategory(userUUID, category, internalPage, size);
            long total = getNotificationsUseCase.countByUserIdAndCategory(userUUID, category);
            return new PaginationResponse(page, size, total,
                    items.stream()
                            .map(InAppNotificationResponse::fromEntity)
                            .collect(Collectors.toList()));
        });
    }

    @Mutation("ReadNotification")
    @Description("Get notification detail and mark it as read if it was unread")
    public Uni<InAppNotificationResponse> readNotification(
            @Name("notificationId") String notificationId,
            @Name("userId") String userId) {
        return Uni.createFrom().item(() -> {
            LOG.infof("GraphQL Mutation ReadNotification: notificationId=%s, userId=%s", notificationId, userId);
            InAppNotificationEntity entity = readNotificationUseCase.readNotification(
                    UUID.fromString(notificationId), UUID.fromString(userId));
            return InAppNotificationResponse.fromEntity(entity);
        });
    }

    @Inject
    com.backoffice.backoffice.infrastructure.repository.DeviceTokenRepository deviceTokenRepository;

    @Query("GetUnreadCountByCategory")
    @Description("Get unread notification count for each category for a user")
    public Uni<List<InAppNotificationCategoryUnreadCountResponse>> getUnreadCountByCategory(
            @Name("userId") String userId) {
        // Simplified - return empty list for now
        return Uni.createFrom().item(List.of());
    }

    @Mutation("RegisterDevice")
    @Description("Register or update a user's device token for push notifications")
    @jakarta.transaction.Transactional
    public Uni<Boolean> registerDevice(
            @Name("userId") String userId,
            @Name("token") String token,
            @Name("platform") String platform) {
        return Uni.createFrom().item(() -> {
            LOG.infof("GraphQL Mutation RegisterDevice: userId=%s, platform=%s", userId, platform);
            UUID userUUID = UUID.fromString(userId);
            
            // Check if token already exists
            com.backoffice.backoffice.domain.notification.DeviceTokenEntity existing = 
                    deviceTokenRepository.findByToken(token);
            
            if (existing != null) {
                existing.setUserId(userUUID);
                existing.setPlatform(platform.toUpperCase());
                deviceTokenRepository.persist(existing);
            } else {
                com.backoffice.backoffice.domain.notification.DeviceTokenEntity newToken = 
                        com.backoffice.backoffice.domain.notification.DeviceTokenEntity.builder()
                                .userId(userUUID)
                                .token(token)
                                .platform(platform.toUpperCase())
                                .build();
                deviceTokenRepository.persist(newToken);
            }
            return true;
        });
    }
}