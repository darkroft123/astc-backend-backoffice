package com.backoffice.backoffice.infrastructure.messaging;

import com.backoffice.backoffice.domain.entity.OutboxEventEntity;
import com.backoffice.backoffice.infrastructure.repository.OutboxEventRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class OutboxEventProcessor {

    private static final Logger LOG = Logger.getLogger(OutboxEventProcessor.class);

    @Inject
    OutboxEventRepository outboxEventRepository;

    @Scheduled(every = "10s")
    @Transactional
    void processOutbox() {

        // ⚠️ ESTE MÉTODO DEBE EXISTIR EN TU REPOSITORY
        List<OutboxEventEntity> events = outboxEventRepository.findUnprocessed(50);

        for (OutboxEventEntity event : events) {

            LOG.infof(
                    "Publishing outbox event: %s / %s / %s",
                    event.eventType,
                    event.aggregateType,
                    event.aggregateId);

            // TODO: publish to RabbitMQ / Kafka

            event.processedAt = LocalDateTime.now();

            outboxEventRepository.save(event);
        }
    }
}