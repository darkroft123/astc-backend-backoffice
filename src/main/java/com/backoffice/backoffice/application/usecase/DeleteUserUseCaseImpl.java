package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.DeleteUserUseCase;
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
import java.util.UUID;

@ApplicationScoped
public class DeleteUserUseCaseImpl implements DeleteUserUseCase {

    private static final Logger LOG = Logger.getLogger(DeleteUserUseCaseImpl.class);

    @Inject
    UserRepository userRepository;

    @Inject
    OutboxEventRepository outboxRepository;

    // @Override
    // @Transactional
    // public boolean execute(UUID id) {

    //     UserEntity user = userRepository.findById(id);
    //     if (user == null) throw new RuntimeException("User not found");

    //     user.setDeletedAt(LocalDateTime.now());

    //     OutboxEventEntity event = new OutboxEventEntity();
    //     event.setId(UuidGenerator.generate());
    //     event.setAggregateType("USER");
    //     event.setAggregateId(id);
    //     event.setEventType("USER_DELETED");
    //     event.setPayload("{\"id\":\"" + id + "\"}");
    //     event.setCreatedAt(LocalDateTime.now());

    //     outboxRepository.persist(event);

    //     return true;
    // }

    @Override
    @Transactional
    public boolean execute(UUID id) {

        UserEntity entity = userRepository.findById(id);

        if (entity == null) {
            throw new RuntimeException("User not found");
        }

        OutboxEventEntity event = new OutboxEventEntity();
        event.setId(UuidGenerator.generate());
        event.setAggregateType("USER");
        event.setAggregateId(id);
        event.setEventType("USER_DELETED");
        event.setPayload("{\"id\":\"" + id + "\"}");
        event.setCreatedAt(LocalDateTime.now());
        outboxRepository.persist(event);
        LOG.infof("[OUTBOX] User DELETED event persisted: userId=%s", id);

        userRepository.delete(entity);

        return true;
    }
}