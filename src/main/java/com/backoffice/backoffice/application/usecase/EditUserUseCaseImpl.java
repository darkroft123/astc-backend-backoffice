package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.EditUserUseCase;
import com.backoffice.backoffice.domain.model.User;
import com.backoffice.backoffice.domain.entity.UserEntity;
import com.backoffice.backoffice.domain.entity.OutboxEventEntity;
import com.backoffice.backoffice.infrastructure.repository.UserRepository;
import com.backoffice.backoffice.infrastructure.repository.OutboxEventRepository;
import com.backoffice.shared.util.UuidGenerator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class EditUserUseCaseImpl implements EditUserUseCase {

    private static final Logger LOG = Logger.getLogger(EditUserUseCaseImpl.class);

    @Inject
    UserRepository userRepository;

    @Inject
    OutboxEventRepository outboxRepository;

    @Override
    @Transactional
    public User execute(UUID id, User user) {

        UserEntity entity = userRepository.findById(id);

        if (entity == null) {
            throw new RuntimeException("User not found");
        }

        boolean roleChanged = false;
        UUID previousRoleId = entity.getRoleId();

        if (user.getUsername() != null)
            entity.setUsername(user.getUsername());

        if (user.getFirstName() != null)
            entity.setFirstName(user.getFirstName());

        if (user.getLastName() != null)
            entity.setLastName(user.getLastName());

        if (user.getEmail() != null)
            entity.setEmail(user.getEmail());

        if (user.getPhone() != null)
            entity.setPhone(user.getPhone());

        if (user.getAvatarUrl() != null)
            entity.setAvatarUrl(user.getAvatarUrl());

        if (user.getRoleId() != null) {
            entity.setRoleId(user.getRoleId());
            roleChanged = !user.getRoleId().equals(previousRoleId);
        }

        entity.setUpdatedAt(Instant.now().atZone(ZoneOffset.UTC).toLocalDateTime());

        String eventType = roleChanged ? "USER_ROLE_CHANGED" : "USER_UPDATED";
        OutboxEventEntity event = new OutboxEventEntity();
        event.setId(UuidGenerator.generate());
        event.setAggregateType("USER");
        event.setAggregateId(id);
        event.setEventType(eventType);
        event.setPayload(buildPayload(entity));
        event.setCreatedAt(utcNow());
        outboxRepository.persist(event);
        LOG.infof("[OUTBOX] User %s event persisted: userId=%s, email=%s", eventType, id, user.getEmail());

        return toDomain(entity);
    }

    private LocalDateTime utcNow() {
        return Instant.now().atZone(ZoneOffset.UTC).toLocalDateTime();
    }

    private String buildPayload(UserEntity u) {
        StringBuilder payload = new StringBuilder();
        payload.append("{");
        payload.append("\"id\":\"").append(u.getId()).append("\"");
        payload.append(",\"email\":\"").append(escape(u.getEmail())).append("\"");
        payload.append(",\"role\":\"").append(escape(u.getRoleId() != null ? u.getRoleId().toString() : "")).append("\"");
        payload.append("}");
        return payload.toString();
    }

    private String escape(String s) {
        return s != null ? s.replace("\\", "\\\\").replace("\"", "\\\"") : "";
    }

    private User toDomain(UserEntity e) {
        User u = new User();
        u.setId(e.getId());
        u.setUsername(e.getUsername());
        u.setEmail(e.getEmail());
        u.setFirstName(e.getFirstName());
        u.setLastName(e.getLastName());
        u.setFullName(e.getFullName());
        u.setPhone(e.getPhone());
        u.setAvatarUrl(e.getAvatarUrl());
        u.setRoleId(e.getRoleId());
        return u;
    }
}