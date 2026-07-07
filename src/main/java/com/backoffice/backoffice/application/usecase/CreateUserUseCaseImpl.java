package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.CreateUserUseCase;
import com.backoffice.backoffice.domain.model.User;
import com.backoffice.backoffice.domain.entity.UserEntity;
import com.backoffice.backoffice.infrastructure.repository.UserRepository;
import com.backoffice.backoffice.infrastructure.repository.OutboxEventRepository;
import com.backoffice.backoffice.domain.entity.OutboxEventEntity;
import com.backoffice.shared.util.UuidGenerator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private static final Logger LOG = Logger.getLogger(CreateUserUseCaseImpl.class);

    @Inject
    UserRepository userRepository;

    @Inject
    OutboxEventRepository outboxRepository;

    @Override
    @Transactional
    public UUID execute(User user) {

        // 1. Generar ID
        UUID id = UUID.randomUUID();

        // 2. MAP DOMAIN -> ENTITY
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setFullName(
            ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
            (user.getLastName() != null ? user.getLastName() : "")).trim()
        );
        entity.setPhone(user.getPhone());
        entity.setAvatarUrl(user.getAvatarUrl());
        entity.setRoleId(user.getRoleId());
        entity.setCreatedAt(utcNow());


        // 🔥 FIX 3: HASH PASSWORD AQUÍ
        entity.setPasswordHash(
            BCrypt.hashpw(user.getPassword(), BCrypt.gensalt())
        );
        // 3. Guardar usuario
        userRepository.persist(entity);

        // 4. OUTBOX EVENT (IMPORTANTE)
        OutboxEventEntity event = new OutboxEventEntity();
        event.setId(UuidGenerator.generate());
        event.setAggregateType("USER");
        event.setAggregateId(id);
        event.setEventType("USER_CREATED");
        event.setPayload(buildPayload(entity));
        event.setCreatedAt(utcNow());

        outboxRepository.persist(event);
        LOG.infof("[OUTBOX] User CREATED event persisted: userId=%s, email=%s, eventType=%s",
                id, user.getEmail(), "USER_CREATED");

        // 5. RETURN ID
        return id;
    }

    private LocalDateTime utcNow() {
        return Instant.now().atZone(ZoneOffset.UTC).toLocalDateTime();
    }

    private String buildPayload(UserEntity u) {
        return "{ " +
                "\"id\": \"" + u.getId() + "\"," +
                "\"email\": \"" + u.getEmail() + "\"" +
                " }";
    }
}