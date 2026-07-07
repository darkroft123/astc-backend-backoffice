package com.backoffice.backoffice.infrastructure.repository;

import com.backoffice.backoffice.domain.entity.OutboxEventEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class OutboxEventRepository implements PanacheRepository<OutboxEventEntity> {

    private static final Logger LOG = Logger.getLogger(OutboxEventRepository.class);

    public List<OutboxEventEntity> findUnprocessed(int limit) {
        List<OutboxEventEntity> results = find("processedAt is null")
                .page(0, limit)
                .list();
        if (!results.isEmpty()) {
            LOG.infof("[OUTBOX] Found %d unprocessed events in backoffice", results.size());
        }
        return results;
    }
}