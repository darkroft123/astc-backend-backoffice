package com.backoffice.backoffice.infrastructure.messaging;

import com.backoffice.backoffice.domain.entity.OutboxEventEntity;
import com.backoffice.backoffice.infrastructure.repository.OutboxEventRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import io.quarkus.scheduler.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class OutboxEventProcessor {

    @Inject
    OutboxEventRepository repository;

    // @Inject
    // RabbitMQPublisher publisher;

    /**
     * 🔥 Se ejecuta automáticamente cada 5 segundos
     */
    @Scheduled(every = "5s")
    @Transactional
    void processEvents() {

        List<OutboxEventEntity> events = repository.findUnprocessed(50);

        for (OutboxEventEntity event : events) {

            try {
                // 🔥 Enviar a RabbitMQ REAL
                // publisher.publish(event.getPayload());

                // 🔥 marcar como procesado
                event.setProcessedAt(LocalDateTime.now());

            } catch (Exception e) {

                // 🔥 importante: no marcar si falla
                System.err.println("❌ Error enviando evento: " + event.getId());
            }
        }
    }
}