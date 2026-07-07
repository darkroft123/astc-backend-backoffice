package com.backoffice.backoffice.infrastructure.messaging;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import com.backoffice.backoffice.domain.entity.OutboxEventEntity;
import com.backoffice.backoffice.infrastructure.repository.OutboxEventRepository;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OutboxEventProcessor {

    private static final Logger LOG = Logger.getLogger(OutboxEventProcessor.class);

    @Inject
    OutboxEventRepository repository;

    @Inject
    @Channel("notification-events")
    Emitter<String> notificationEmitter;

    @Inject
    Vertx vertx;

    void init(@Observes StartupEvent ev) {
        vertx.setPeriodic(5000, id -> vertx.executeBlocking(
            promise -> {
                try {
                    processEvents();
                    promise.complete();
                } catch (Exception e) {
                    LOG.errorf(e, "Error processing outbox events");
                    promise.fail(e);
                }
            },
            false
        ));
    }

    @Transactional
    void processEvents() {
        List<OutboxEventEntity> events = repository.findUnprocessed(50);
        if (events.isEmpty()) {
            return;
        }

        LOG.infof("📬 [Outbox] Found %d unprocessed outbox events to process in backoffice-service", events.size());

        for (OutboxEventEntity event : events) {
            try {
                LOG.infof("📬 [Outbox] Processing event ID=%s, type=%s, payload=%s", 
                        event.getId(), event.getEventType(), event.getPayload());
                JsonObject payload = new JsonObject(event.getPayload());
                String userId = payload.getString("id");

                String templateCode = mapEventType(event.getEventType());
                if (templateCode == null) {
                    LOG.warnf("⚠ [Outbox] Unknown event type '%s' for outbox event %s, skipping",
                            event.getEventType(), event.getId());
                    event.setProcessedAt(LocalDateTime.now());
                    continue;
                }

                JsonObject variables = new JsonObject();
                variables.put("USER_ID", userId);
                if (payload.containsKey("email")) {
                    variables.put("USER_EMAIL", payload.getString("email"));
                }
                if (payload.containsKey("role")) {
                    variables.put("ROLE_NAME", payload.getString("role"));
                }

                String toEmail = payload.getString("email", "dev-notification@astc.local");
                String toName = payload.getString("username", "");

                JsonObject msg = new JsonObject();
                msg.put("notificationCode", templateCode);
                msg.put("type", "EMAIL");
                msg.put("userId", userId);
                msg.put("toEmail", toEmail);
                msg.put("toName", toName);
                msg.put("variables", variables);

                LOG.infof("📬 [Outbox] Dispatching payload to RabbitMQ channel 'notification-events': %s", msg.encode());
                notificationEmitter.send(msg.encode());
                LOG.infof("✅ [Outbox] Successfully published outbox event %s (type=%s) as %s to=%s",
                        event.getId(), event.getEventType(), templateCode, toEmail);

                event.setProcessedAt(LocalDateTime.now());
            } catch (Exception e) {
                LOG.errorf(e, "❌ [Outbox] Failed to process outbox event %s (type=%s)",
                        event.getId(), event.getEventType());
            }
        }
    }

    private String mapEventType(String eventType) {
        switch (eventType) {
            case "USER_CREATED":
                return "ASB000001";
            case "USER_UPDATED":
                return "ASB000002";
            case "USER_ROLE_CHANGED":
                return "ASB000003";
            case "USER_DELETED":
                return "ASB000004";
            default:
                return null;
        }
    }
}
